var exec = require('cordova/exec');


/**
 * Temporary variables
 */

/**
 * In the DOM API, the browser or browser plugin is responsible for supplying any available channel binding information to the FIDO Client.
 * So this is temporary global polyfill
 * @type {Object}
 */
var channelBindings = {
    "serverEndPoint" : null,
    "tlsServerCertificate" : null,
    "tlsUnique" : null,
    "cid_pubkey" : null
};

/**
 * Same here
 * @type {String}
 */
var origin = "https://example.com/"

/**
 * These functions are basically convenience functions for cordova.exec()
 * They bind to the 'clobbers' target defined in the plugin.xml file, such that
 * uafDiscover() becomes fido.uafDiscover() in the Cordova application
 */
window.navigator.fido = {
    uaf : {
        discover: function(completionCallback, errorCallback) {
            exec(

                (data) => {
                    completionCallback(JSON.parse(data))
                }, // success

                errorCallback,  // fail

                "fido",
                "uafDiscover",
                []
            );
        },

        checkPolicy: function(message, errorCallback) {
            exec(
                /**
                 * checkPolicy must always return exception. The only difference is status code.
                 */
                errorCallback, // success
                errorCallback,  // fail
                "fido",
                "uafCheckPolicy",
                [ message, origin ]
            );
        },

        processUAFOperation: function(message, completionCallback, errorCallback) {
            exec(
                (data) => {
                    completionCallback(JSON.parse(data))
                }, // success
                errorCallback, // fail
                "fido",
                "uafOperation",
                [ message, channelBindings, origin ]
            );
        },

        notifyUAFResult: function(responseCode, uafResponse) {
        }
    }
};

module.exports = navigator.fido;