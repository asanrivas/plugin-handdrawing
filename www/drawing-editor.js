var exec = require('cordova/exec');

var handdrawing = {
  openDraw: function(url, successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'DrawingEditor','openDraw', [ url ]);
  }
}

module.exports = handdrawing;
