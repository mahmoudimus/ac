var RA = (function() {
    var RA = RA || {};
    var socket;
    RA.onMessage = function (message, origin) {
      // Replace to handle messages from parent frame.
    }

    RA.init = function(options) {
        socket = new easyXDM.Socket({
            onMessage:function(message, origin) {
              RA.onMessage(message, origin);
            }
        });
        socket.postMessage(JSON.stringify({
            id : 'init'
        }));

    };

    RA.resize = function(width, height) {
        var w = width || "100%";
        var h = height || (document.body.offsetHeight + 40);
        socket.postMessage(JSON.stringify({
                id : 'resize',
                width : w,
                height : h
        }));
    };

    RA.getCurrentIssue = function() {
        socket.postMessage(JSON.stringify({
                id : 'getIssue'
        }));
    };

    RA.getCurrentUsername = function() {
      socket.postMessage(JSON.stringify({
              id : 'getUsername'
      }));
  };

    return RA;
})();


