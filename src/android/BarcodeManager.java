package com.datalogic.cordova.decode;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.datalogic.decode.PropertyID;

//import com.datalogic.decode.BarcodeManager;
import com.datalogic.decode.DecodeException;
import com.datalogic.decode.DecodeResult;
import com.datalogic.decode.ReadListener;
import com.datalogic.device.ErrorManager;
import com.datalogic.decode.BarcodeID;

import android.util.Log;
import android.os.Bundle;


public class BarcodeManager extends CordovaPlugin {

    private final String LOGTAG = getClass().getName();
    com.datalogic.decode.BarcodeManager decoder = null;
    ReadListener listener = null;
    CallbackContext callbackContext = null;
    boolean multiTasking=true;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView){
        super.initialize(cordova, webView);
        //initializeDecoder();
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext context) throws JSONException {

        if (action.equals("addReadListner")) {
            callbackContext = context;
            // PluginResult result = new PluginResult(PluginResult.Status.OK, message);
            // result.setKeepCallback(true);
            // if(barcodeData == null){
            //     callbackContext.sendPluginResult(result);    
            // }
            return true;

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
        Bundle state = new Bundle();
        return state;
    }

    @Override
    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext){
        this.callbackContext = callbackContext;
    }

    @Override
    public void onResume(boolean multiTasking){
        super.onResume(multiTasking);
        Log.i(LOGTAG, "onResume");

        // From here on, we want to be notified with exceptions in case of errors.
        ErrorManager.enableExceptions(true);

        // If the decoder instance is null, create it.
        if (decoder == null) { // Remember an onPause call will set it to null.
            decoder = new com.datalogic.decode.BarcodeManager();
        }

        try {

            // Create an anonymous class.
            listener = new ReadListener() {

                // Implement the callback method.
                @Override
                public void onRead(DecodeResult decodeResult) {
                    // Change the result to the current received result.
                    try{
                        JSONObject barcodeObject = new JSONObject();
                        barcodeObject.put("barcodeData", decodeResult.getText());
                        barcodeObject.put("barcodeType", decodeResult.getBarcodeID().name());

                        if(callbackContext != null){
                            PluginResult result = new PluginResult(PluginResult.Status.OK, barcodeObject.toString());
                            result.setKeepCallback(true);
                            callbackContext.sendPluginResult(result);
                        }
                    }
                    catch(Exception e){
                        Log.e(LOGTAG, "Error while trying to read barcode data", e);
                    }
                    
                }

            };

            // Remember to add it, as a listener.
            decoder.addReadListener(listener);

        } catch (DecodeException e) {
            Log.e(LOGTAG, "Error while trying to bind a listener to BarcodeManager", e);
        }
    }

    @Override
    public void onPause(boolean multiTasking) {
        super.onPause(multiTasking);

        Log.i(LOGTAG, "onPause");

        // If we have an instance of BarcodeManager.
        if (decoder != null) {
            try {
                // Unregister our listener from it and free resources.
                decoder.removeReadListener(listener);

                // Let the garbage collector take care of our reference.
                decoder = null;

            } catch (Exception e) {
                Log.e(LOGTAG, "Error while trying to remove a listener from BarcodeManager", e);
            }
        }
    }

    private boolean pressTrigger(CallbackContext callbackContext){
        if(decoder != null){
            try{
                if (decoder.pressTrigger() == DecodeException.SUCCESS) {
                    PluginResult result = new PluginResult(PluginResult.Status.OK, "Successfully pressed the trigger");
                    callbackContext.sendPluginResult(result);
                    return true;
                }
            } catch(DecodeException e) {
                Log.e(LOGTAG, "Error while pressing the trigger", e);
            }
        }
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Error while pressing the trigger");
        callbackContext.sendPluginResult(result);
        return false;
    }

    private boolean releaseTrigger(CallbackContext callbackContext){
        if(decoder != null){
            try{
                if (decoder.releaseTrigger() == DecodeException.SUCCESS) {
                    PluginResult result = new PluginResult(PluginResult.Status.OK, "Successfully released the trigger");
                    callbackContext.sendPluginResult(result);
                    return true;
                }
            } catch(DecodeException e) {
                Log.e(LOGTAG, "Error while releasing the trigger", e);     
            }
        }
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Error while releasing the trigger");
        callbackContext.sendPluginResult(result);
        return false;
    }
    
}