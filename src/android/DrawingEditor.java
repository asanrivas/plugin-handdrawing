package com.yeayyy.handrawing;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;

/**
 * Created by asanrivas on 19/06/2015.
 */

public class DrawingEditor extends CordovaPlugin {

    @Override
    public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        Log.v("DrawingActivity", "entered my domain");
        if (action.compareTo("openDraw") == 0)
        {
            String message = args.getString(0);
            this.echo(message, callbackContext);

            Intent i = new Intent();
            Context context=this.cordova.getActivity().getApplicationContext();
            //or Context context=cordova.getActivity().getApplicationContext();
            Intent intent=new Intent(context,MainActivity.class);

            //context.startActivity(intent);
            cordova.getActivity().startActivity(intent);
            return true;
        }

        return false;
    }

    private void echo(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

}