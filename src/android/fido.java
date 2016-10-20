package org.fidoalliance.mobile;

import android.util.Log;
import android.content.Intent;
import android.app.Activity;
import android.content.ActivityNotFoundException;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.pm.PackageManager;
import java.util.List;
import android.content.pm.ResolveInfo;
import android.content.pm.ApplicationInfo;

import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * This class echoes a string called from JavaScript.
 */
public class fido extends CordovaPlugin {

    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Log.w("FIDO", "executing...");
        this.callbackContext = callbackContext;

        if (action.equals("uafDiscover")) {
            return this.uafDiscover();
        } else if (action.equals("uafCheckPolicy")) {
            String message = args.getString(0);
            return this.uafCheckPolicy(message);
        } else if (action.equals("uafOperation")) {
            String message = args.getString(0);
            return this.uafOperation(message);
        } else {
            Log.w("FIDO", "Unknown FIDO Cordova command: " + action);
            return false;
        }
    }

    // TODO: there's a lot of copying and pasting below -- this could probably be refactored
    private boolean uafDiscover() {
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

        /**
         * We can't call the callback until we get a resonse to our intent
         * So say "NO_RESULT" for now and save the result in our context
         */
        PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
        r.setKeepCallback(true);
        this.callbackContext.sendPluginResult(r);

        return true;
    }

    private void uafDiscoverResult(Intent data) {
        Log.d("FIDO", "uafDiscoverResult");
        /**
         * Per UAF Client API Transport, Section 6.2.2
         */
        String componentName = data.getStringExtra("componentName");
        Log.d ("FIDO", "Component Name: " + componentName);
        String errorCode = data.getStringExtra("errorCode"); // XXX wrong
        Log.d ("FIDO", "Error Code: " + errorCode);
        String discoveryData = data.getStringExtra("discoveryData");
        Log.d ("FIDO", "Discovery Data: " + discoveryData);
        // TODO: Discovery needs to reutnr discoveryData and an errorCode
        this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, discoveryData));
    }

    // private boolean uafCheckPolicy(String message, String origin) {
    private boolean uafCheckPolicy(String message) {
        Log.d("FIDO", "uafCheckPolicy: " + message);
        if (message != null && message.length() > 0) {
            // this.cordova.startActivityForResult((CordovaPlugin) this,i, 0);
            callbackContext.success("{\"uafCheckPolicy\": \"" + message + "\"}");
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }

        return true;
    }

    private void uafCheckPolicyResult(Intent data) {
        Log.d("FIDO", "uafCheckPolicyResult");
    }

    // private boolean uafOperation(String message, String channelBindings, String origin) {
    private boolean uafOperation(String message) {
        Log.d("FIDO", "uafOperation: " + message);

        /**
         * Fire our FIDO_OPERATION::UAF_OPERATION
         * Hopefully onActivityResult() below will catch a reply
         * If nothing is found, startActivityForResult() will throw an ActivityNotFoundException error
         */
        Intent fidoIntent = new Intent("org.fidoalliance.intent.FIDO_OPERATION");
        fidoIntent.setType ("application/fido.uaf_client+json");
        fidoIntent.putExtra ("UAFIntentType", "UAF_OPERATION");

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

        /**
         * We can't call the callback until we get a resonse to our intent
         * So say "NO_RESULT" for now and save the result in our context
         */
        PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
        r.setKeepCallback(true);
        this.callbackContext.sendPluginResult(r);

        return true;
    }

    private void uafOperationResult(Intent data) {
        Log.d("FIDO", "uafOperationResult");

        Log.d ("FIDO", data.toString());
        String errorCode = data.getStringExtra("errorCode"); // XXX wrong
        Log.d ("FIDO", "Error Code: " + errorCode);
        String message = data.getStringExtra("message");
        Log.d ("FIDO", "Message: " + message);
        this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, message));
    }

    private void uafOperationCompletionStatus(Intent data) {
        Log.d("FIDO", "uafOperationCompletionStatus");

        String componentName = data.getStringExtra("componentName");
        Log.d ("FIDO", "Component Name: " + componentName);
        String responseCode = data.getStringExtra("responseCode"); // XXX wrong
        Log.d ("FIDO", "Response Code: " + responseCode);
        String message = data.getStringExtra("message");
        Log.d ("FIDO", "Message: " + message);
        this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, message));
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
            String intentType = data.getStringExtra("UAFIntentType");
            Log.d ("FIDO", "Intent Type: " + intentType);
            if (intentType.equals("DISCOVER_RESULT")) {
                uafDiscoverResult(data);
                return;
            } else if (intentType.equals("CHECK_POLICY_RESULT")) {
                uafCheckPolicyResult(data);
                return;
            } else if (intentType.equals("UAF_OPERATION_RESULT")) {
                uafOperationResult(data);
                return;
            } else if (intentType.equals("UAF_OPERATION_COMPLETION_STATUS")) {
                uafOperationCompletionStatus(data);
                return;
            } else {
                Log.w ("FIDO", "Unknown UAFIntentType: " + intentType);                    
            }
        }
    }
}
