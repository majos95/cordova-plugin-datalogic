package com.datalogic.cordova.decode;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.datalogic.decode.PropertyID;
import com.datalogic.decode.DecodeException;
import com.datalogic.decode.DecodeResult;
import com.datalogic.device.ErrorManager;
import com.datalogic.decode.BarcodeID;
import com.datalogic.decode.ReadListener;

import android.util.Log;
import android.os.Bundle;

public class BarcodeManager extends CordovaPlugin {

    private final String LOGTAG = getClass().getName();
    private com.datalogic.decode.BarcodeManager decoder = null;
    private ReadListener listener = null;
    private CallbackContext callbackContext = null;
    private boolean multiTasking = true;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView){
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext context) throws JSONException {
        if (action.equals("addReadListener")) {
            return addReadListener(context);

        } else if (action.equals("removeReadListener")) {
            return removeReadListener(context);

        } else if (action.equals("pressTrigger")) {
            return pressTrigger(context);

        } else if (action.equals("releaseTrigger")) {
            return releaseTrigger(context);

        } else {
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

        // Re-establish listener if needed
        if (callbackContext != null) {
            addReadListener(callbackContext);
        }
    }

    @Override
    public void onPause(boolean multiTasking) {
        super.onPause(multiTasking);
        Log.i(LOGTAG, "onPause");

        if (decoder != null) {
            try {
                decoder.removeReadListener(listener);
                decoder = null;
            } catch (Exception e) {
                Log.e(LOGTAG, "Error while trying to remove a listener from BarcodeManager", e);
            }
        }
    }

    private boolean addReadListener(CallbackContext callbackContext) {
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
                        Log.e(LOGTAG, "Error while trying to read barcode data", e);
                    }
                }
            };

            decoder.addReadListener(listener);

            PluginResult result = new PluginResult(PluginResult.Status.OK, "Read listener successfully added");
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
            return true;

        } catch (Exception e) {
            Log.e(LOGTAG, "Error while setting up read listener", e);
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Failed to add read listener");
            callbackContext.sendPluginResult(result);
            return false;
        }
    }

    private boolean removeReadListener(CallbackContext callbackContext) {
        try {
            if (decoder != null && listener != null) {
                decoder.removeReadListener(listener);
                listener = null;
                PluginResult result = new PluginResult(PluginResult.Status.OK, "Listener successfully removed");
                callbackContext.sendPluginResult(result);
            } else {
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, "No listener to remove");
                callbackContext.sendPluginResult(result);
            }
            return true;
        } catch (Exception e) {
            Log.e(LOGTAG, "Error while trying to remove listener", e);
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Failed to remove listener");
            callbackContext.sendPluginResult(result);
            return false;
        }
    }

    private boolean pressTrigger(CallbackContext callbackContext){
        if (decoder != null){
            try {
                if (decoder.pressTrigger() == DecodeException.SUCCESS) {
                    PluginResult result = new PluginResult(PluginResult.Status.OK, "Successfully pressed the trigger");
                    callbackContext.sendPluginResult(result);
                    return true;
                }
            } catch (DecodeException e) {
                Log.e(LOGTAG, "Error while pressing the trigger", e);
            }
        }
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Error while pressing the trigger");
        callbackContext.sendPluginResult(result);
        return false;
    }

    private boolean releaseTrigger(CallbackContext callbackContext){
        if (decoder != null){
            try {
                if (decoder.releaseTrigger() == DecodeException.SUCCESS) {
                    PluginResult result = new PluginResult(PluginResult.Status.OK, "Successfully released the trigger");
                    callbackContext.sendPluginResult(result);
                    return true;
                }
            } catch (DecodeException e) {
                Log.e(LOGTAG, "Error while releasing the trigger", e);
            }
        }
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Error while releasing the trigger");
        callbackContext.sendPluginResult(result);
        return false;
    }
}
