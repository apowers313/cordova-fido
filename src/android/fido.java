package org.fidoalliance.mobile;

import android.util.Log;
import android.content.Intent;
import android.app.Activity;
import android.content.ActivityNotFoundException;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.pm.PackageManager;
import java.util.List;
import android.content.pm.ResolveInfo;
import android.content.pm.ApplicationInfo;

/**
 * This class echoes a string called from JavaScript.
 */
public class fido extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.w("FIDO", "executing...");
        if (action.equals("uafDiscover")) {
            String message = args.getString(0);
            this.uafDiscover(message, callbackContext);
            return true;
        }
        return false;
    }

    private void uafDiscover(String message, CallbackContext callbackContext) {
        /**
         * Lookup Packages that provide the intent
         * Per UAF Client API Transport, Section 6.2 (See: "NOTE")
         */ 
        // PackageManager pm = getPackageManager();
        // 0x00020000 == MATCH_ALL
        // List<ResolveInfo> resolveInfos = pm.queryIntentActivities (fidoIntent, 0x00020000);
        // Log.d("FIDO", "Num Packages Found: " + resolveInfos.size());
        // for(ResolveInfo info : resolveInfos) {
        //     ApplicationInfo applicationInfo = info.activityInfo.applicationInfo;
        //     // Note: on Samsung Galaxy S6, this isn't very interesting
        //     // Maybe because it's all in the System rather than a user-land application?
        //     Log.d("FIDO", "Application Name: " + applicationInfo.name);
        //     Log.d("FIDO", "Application Class: " + applicationInfo.className);
        //     Log.d("FIDO", "Application Label: " + applicationInfo.labelRes);
        //     Log.d("FIDO", "Application Package: " + applicationInfo.packageName);
        //     Log.d("FIDO", "Application nonLocalizedLabel: " + applicationInfo.nonLocalizedLabel);
        // }
        // XXX stupid PackageManager code isn't linking in... TODO fix later

        /**
         * Create Intent for FIDO_OPERATION::DISCOVER
         * Per UAF Client API Transport, Section 6.2.1
         */
        Log.d("FIDO", "creating intent");
        Intent fidoIntent = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
        fidoIntent.setType ("application/fido.uaf_client+json");
        fidoIntent.putExtra ("UAFIntentType", "DISCOVER");
        Log.d("FIDO", "intent created.");

        /**
         * Fire our FIDO_OPERATION::DISCOVER
         * Hopefully onActivityResult() below will catch a reply
         * If nothing is found, startActivityForResult() will throw an ActivityNotFoundException error
         */
        Log.d("FIDO", "setting callback");
        cordova.setActivityResultCallback (this);
        try {
            Log.d("FIDO", "starting activity");
            cordova.getActivity().startActivityForResult(fidoIntent, 1);
        } catch (ActivityNotFoundException e) {
            Log.e("FIDO", "Got 'Not Found' exception");
            Log.d("FIDO", "Exception: " + e.getMessage());
        }
        Log.d("FIDO", "blah blah blah");

        Log.w("FIDO", "uafDiscover...");
        if (message != null && message.length() > 0) {
            // this.cordova.startActivityForResult((CordovaPlugin) this,i, 0);
            callbackContext.success("{\"testing\": \"" + message + "\"}");
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    @Override
    /**
     * Catch any replies to intents that we sent out
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("FIDO", "Got Activity Result");
        Log.d("FIDO", "Request Code: " + requestCode);
        Log.d("FIDO", "Result Code: " + resultCode);

        /**
         * This is a FIDO_OPERATION UAF Intent (as opposed to other Intents we might catch)
         */
        if (data.getExtras().containsKey("UAFIntentType")) {
            Log.d ("FIDO", "Received UAFIntentType:");
            /**
             * We should only be catching DISCOVERY_RESULTS
             * Otherwise the code below will choke -- we can make this more robust later
             * Per UAF Client API Transport, Section 6.2.2
             */
            String intentType = data.getStringExtra("UAFIntentType");
            Log.d ("FIDO", "Intent Type: " + intentType);
            String componentName = data.getStringExtra("componentName");
            Log.d ("FIDO", "Component Name: " + componentName);
            String errorCode = data.getStringExtra("errorCode"); // XXX wrong
            Log.d ("FIDO", "Error Code: " + errorCode);
            String discoveryData = data.getStringExtra("discoveryData");
            Log.d ("FIDO", "Discovery Data: " + discoveryData);
        }
    }
}
