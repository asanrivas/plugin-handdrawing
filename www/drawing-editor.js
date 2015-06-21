var exec = require('cordova/exec');
console.log('called handdrawing');

var handdrawing = {
  openDraw: function(successCallback, errorCallback, url) {
    exec(successCallback, errorCallback, 'DrawingEditor','openDraw', [ url ]);
  }
}
module.exports = handdrawing;