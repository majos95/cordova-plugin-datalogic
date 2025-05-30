var exec = require('cordova/exec');

var barcodeManager = function() {};

barcodeManager.addReadListener = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "BarcodeManager", "addReadListener", []);
};

barcodeManager.removeReadListener = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "BarcodeManager", "removeReadListener", []);
};

barcodeManager.pressTrigger = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "BarcodeManager", "pressTrigger", []);
};

barcodeManager.releaseTrigger = function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, "BarcodeManager", "releaseTrigger", []);
};

module.exports = barcodeManager;
