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
    private boolean listenerEnabled = false;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext context) throws JSONException {
        switch (action) {
            case "addReadListner":
                this.callbackContext = context;
                return enableListener();

            case "removeReadListner":
                return disableListener(context);

            case "pressTrigger":
                return pressTrigger(context);

            case "releaseTrigger":
                return releaseTrigger(context);

            default:
                return false;
        }
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        Log.i(LOGTAG, "onResume");

        ErrorManager.enableExceptions(true);

        if (decoder == null) {
            decoder = new com.datalogic.decode.BarcodeManager();
        }

        if (listenerEnabled && listener == null) {
            setupListener();
        }
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        Log.i(LOGTAG, "onPause");

        if (decoder != null && listener != null) {
            try {
                decoder.removeReadListener(listener);
                listener = null;
            } catch (Exception e) {
                Log.e(LOGTAG, "Failed to remove listener", e);
            }
        }

        decoder = null;
    }

    private boolean enableListener() {
        listenerEnabled = true;

        if (decoder == null) {
            decoder = new com.datalogic.decode.BarcodeManager();
        }

        if (listener == null) {
            setupListener();
        }

        // Keep the callback for future scan results
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
        return true;
    }

    private boolean disableListener(CallbackContext context) {
        listenerEnabled = false;

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

    private void setupListener() {
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
                    Log.e(LOGTAG, "Error reading barcode", e);
                }
            }
        };

        try {
            decoder.addReadListener(listener);
            Log.i(LOGTAG, "Listener added");
        } catch (DecodeException e) {
            Log.e(LOGTAG, "Failed to add listener", e);
        }
    }

    private boolean pressTrigger(CallbackContext context) {
        if (decoder != null) {
            try {
                if (decoder.pressTrigger() == DecodeException.SUCCESS) {
                    context.sendPluginResult(new PluginResult(PluginResult.Status.OK, "Trigger pressed"));
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
                    context.sendPluginResult(new PluginResult(PluginResult.Status.OK, "Trigger released"));
                    return true;
                }
            } catch (DecodeException e) {
                Log.e(LOGTAG, "Error releasing trigger", e);
            }
        }

        context.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Error releasing trigger"));
        return false;
    }
}
