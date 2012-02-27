var RA = (function() {
    var RA = RA || {};
    var socket;
    RA.onMessage = function (message, origin) {
      // Client code supplies this function to handle inbound messages from the parent frame.
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

    RA.showMessage = function(messageId, title, body) {
      socket.postMessage(JSON.stringify({
              id : 'showMessage',
              messageId : messageId,
              title : title,
              body : body
      }));
    };

    RA.clearMessage = function(messageId) {
      socket.postMessage(JSON.stringify({
              id : 'clearMessage',
              messageId : messageId
      }));
    };

    return RA;
})();


