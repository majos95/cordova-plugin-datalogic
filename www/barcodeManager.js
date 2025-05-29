
/*global cordova, module*/
var exec = require('cordova/exec');

var barcodeManager = function() {};

barcodeManager.addReadListner = function (successCallback, errorCallback) {
	exec(successCallback, errorCallback, "BarcodeManager", "addReadListner", []);
    
};

barcodeManager.pressTrigger = function (successCallback, errorCallback) {
	exec(successCallback, errorCallback, "BarcodeManager", "pressTrigger", []);
    
};

barcodeManager.releaseTrigger = function (successCallback, errorCallback) {
	exec(successCallback, errorCallback, "BarcodeManager", "releaseTrigger", []);
    
};

module.exports = barcodeManager;