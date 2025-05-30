package com.datalogic.cordova.decode;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.datalogic.decode.DecodeException;
import com.datalogic.decode.DecodeResult;
import com.datalogic.decode.ReadListener;
import com.datalogic.device.ErrorManager;

import android.util.Log;
import android.os.Bundle;

public class BarcodeManager extends CordovaPlugin {

    private final String LOGTAG = getClass().getName();
    private com.datalogic.decode.BarcodeManager decoder = null;
    private ReadListener listener = null;
    private CallbackContext callbackContext = null;
    private boolean listenerAdded = false;


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView){
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext context) throws JSONException {

        switch (action) {
            case "addReadListener":
                return addReadListener(context);
            case "removeReadListener":
                return removeReadListener(context);
            case "pressTrigger":
                return pressTrigger(context);
            case "releaseTrigger":
                return releaseTrigger(context);
            default:
                return false;
        }
    }

    @Override
    public Bundle onSaveInstanceState(){
        return new Bundle();
    }

    @Override
    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext){
        this.callbackContext = callbackContext;
    }

    @Override
    public void onResume(boolean multiTasking){
        super.onResume(multiTasking);
        Log.i(LOGTAG, "onResume");

        ErrorManager.enableExceptions(true);

        if (decoder == null) {
            decoder = new com.datalogic.decode.BarcodeManager();
        }
    }

    @Override
    public void onPause(boolean multiTasking) {
        super.onPause(multiTasking);
        Log.i(LOGTAG, "onPause");

        // Cleanup happens on explicit removeReadListener call
        // Do NOT remove listener automatically here, to support multiple pages
    }

    private boolean addReadListener(CallbackContext context) {
        this.callbackContext = context;

        if (!listenerAdded) {
            try {
                if (decoder == null) {
                    decoder = new com.datalogic.decode.BarcodeManager();
                }

                listener = new ReadListener() {
                    @Override
                    public void onRead(DecodeResult decodeResult) {
                        try {
                            JSONObject barcodeObject = new JSONObject();
                            barcodeObject.put("barcodeData", decodeResult.getText());
                            barcodeObject.put("barcodeType", decodeResult.getBarcodeID().name());

                            if (callbackContext != null) {
                                PluginResult result = new PluginResult(PluginResult.Status.OK, barcodeObject.toString());
                                result.setKeepCallback(true);
                                callbackContext.sendPluginResult(result);
                            }
                        } catch (Exception e) {
                            Log.e(LOGTAG, "Error while reading barcode", e);
                        }
                    }
                };

                decoder.addReadListener(listener);
                listenerAdded = true;

                PluginResult result = new PluginResult(PluginResult.Status.OK, "Read listener added");
                result.setKeepCallback(true);
                context.sendPluginResult(result);
            } catch (DecodeException e) {
                Log.e(LOGTAG, "Error adding listener", e);
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Failed to add read listener");
                context.sendPluginResult(result);
                return false;
            }
        } else {
            PluginResult result = new PluginResult(PluginResult.Status.OK, "Read listener already active");
            result.setKeepCallback(true);
            context.sendPluginResult(result);
        }

        return true;
    }

    private boolean removeReadListener(CallbackContext context) {
        if (decoder != null && listenerAdded && listener != null) {
            try {
                decoder.removeReadListener(listener);
                listener = null;
                listenerAdded = false;

                PluginResult result = new PluginResult(PluginResult.Status.OK, "Read listener removed");
                context.sendPluginResult(result);
                return true;
            } catch (Exception e) {
                Log.e(LOGTAG, "Error removing listener", e);
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Failed to remove read listener");
                context.sendPluginResult(result);
                return false;
            }
        } else {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "No active listener to remove");
            context.sendPluginResult(result);
            return false;
        }
    }

    private boolean pressTrigger(CallbackContext context){
        if (decoder != null) {
            try {
                if (decoder.pressTrigger() == DecodeException.SUCCESS) {
                    PluginResult result = new PluginResult(PluginResult.Status.OK, "Trigger pressed");
                    context.sendPluginResult(result);
                    return true;
                }
            } catch (DecodeException e) {
                Log.e(LOGTAG, "Error pressing trigger", e);
            }
        }

        PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Error pressing trigger");
        context.sendPluginResult(result);
        return false;
    }

    private boolean releaseTrigger(CallbackContext context){
        if (decoder != null) {
            try {
                if (decoder.releaseTrigger() == DecodeException.SUCCESS) {
                    PluginResult result = new PluginResult(PluginResult.Status.OK, "Trigger released");
                    context.sendPluginResult(result);
                    return true;
                }
            } catch (DecodeException e) {
                Log.e(LOGTAG, "Error releasing trigger", e);
            }
        }

        PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Error releasing trigger");
        context.sendPluginResult(result);
        return false;
    }
}
