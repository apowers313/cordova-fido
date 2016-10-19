var exec = require('cordova/exec');

exports.uafDiscover = function(arg0, success, error) {
    console.log ("uafDiscover in fido.js");
    exec(success, error, "fido", "uafDiscover", [arg0]);
    console.log ("uafDiscover done.");
};
