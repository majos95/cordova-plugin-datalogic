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

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext context) throws JSONException {
        switch (action) {
            case "addReadListener":
                return addReadListener(context);

            case "removeReadListener":
                return removeListener(context);

            case "pressTrigger":
                return pressTrigger(context);

            case "releaseTrigger":
                return releaseTrigger(context);

            default:
                return false;
        }
    }

    private boolean addReadListener(CallbackContext context) {
        callbackContext = context;
        ErrorManager.enableExceptions(true);

        try {
            if (decoder == null) {
                decoder = new com.datalogic.decode.BarcodeManager();
            }

            if (listener == null) {
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
                            Log.e(LOGTAG, "Error processing barcode read", e);
                        }
                    }
                };

                decoder.addReadListener(listener);
            }

            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
            return true;

        } catch (Exception e) {
            Log.e(LOGTAG, "Failed to initialize or register listener", e);
            context.error("Error initializing barcode manager or adding listener.");
            return false;
        }
    }

    private boolean removeListener(CallbackContext context) {
        if (decoder != null && listener != null) {
            try {
                decoder.removeReadListener(listener);
                listener = null;
                PluginResult result = new PluginResult(PluginResult.Status.OK, "Listener removed");
                context.sendPluginResult(result);
                return true;
            } catch (Exception e) {
                Log.e(LOGTAG, "Error removing listener", e);
            }
        }
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, "No listener to remove");
        context.sendPluginResult(result);
        return false;
    }

    private boolean pressTrigger(CallbackContext context) {
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
        context.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Error pressing trigger"));
        return false;
    }

    private boolean releaseTrigger(CallbackContext context) {
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
        context.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Error releasing trigger"));
        return false;
    }

    @Override
    public Bundle onSaveInstanceState() {
        return new Bundle();
    }

    @Override
    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }
}
