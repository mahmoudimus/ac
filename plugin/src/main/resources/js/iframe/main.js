var RA = (function() {
    var RA = RA || {};
    var socket;
    RA.init = function(options) {
        socket = new easyXDM.Socket({
            onMessage:function(message, origin) {
                //do something with message
            }
        });
    };

    RA.resize = function(width, height) {
        var w = width || document.body.scrollWidth;
        var h = height || document.body.scrollHeight;
        socket.postMessage( (parseInt(w)) + "," + ( h) );
    };

    return RA;
})();


