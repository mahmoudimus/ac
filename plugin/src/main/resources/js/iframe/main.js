var RA = (function() {
    var RA = RA || {};
    var socket;
    RA.init = function(options) {
        socket = new easyXDM.Socket({
            onMessage:function(message, origin) {
                //do something with message
            }
        });
        socket.postMessage(JSON.stringify({
            id : 'init'
        }));

    };

    RA.resize = function(width, height) {
        var w = width || "100%";
        var h = height || document.body.offsetHeight;
        socket.postMessage(JSON.stringify({
                id : 'resize',
                width : w,
                height : h
        }));
    };

    return RA;
})();


