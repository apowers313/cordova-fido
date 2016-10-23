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
var fido = {
    uaf : {
        discover: function() {
            return new Promise(function(resolve, reject) {
                exec(
                    resolve, // success
                    reject,  // fail
                    "fido",
                    "uafDiscover",
                    []
                );
            });
        },

        checkPolicy: function(message) {
            return new Promise(function(resolve, reject) {
                exec(
                    /**
                     * checkPolicy must always return exception. The only difference is status code.
                     */
                    reject, // success
                    reject,  // fail
                    "fido",
                    "uafCheckPolicy",
                    [ message, origin ]
                );
            });
        },

        processUAFOperation: function(message) {
            return new Promise(function(resolve, reject) {
                exec(
                    resolve, // success
                    reject,  // fail
                    "fido",
                    "uafOperation",
                    [ message, channelBindings, origin ]
                );
            });
        },

        notifyUAFResult: function(responseCode) {
            return new Promise(function(resolve, reject) {
                // TODO Implement notifyUAFResult (int responseCode, UAFMessage uafResponse);
            });
        }
    }
};

module.exports = fido;