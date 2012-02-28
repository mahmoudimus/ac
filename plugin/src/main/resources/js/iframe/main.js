var RA = (function() {
    var RA = RA || {};
    var rpc;
    RA.init = function(options) {
        rpc = new easyXDM.Rpc({}, {
            remote : {
                resize : {},
                init : {},
                getLocation : {},
                getPath : {},
                getUser : {},
                showMessage : {},
                clearMessage : {}
            }
        });
        rpc.init();
    };

    RA.resize = function(width, height) {
        var w = width || "100%";
        var h = height || (document.body.offsetHeight + 40);
        rpc.resize(h, w);
    };

    RA.getLocation = function(fn) {
        return rpc.getLocation(fn);
    };

    RA.getPath = function(fn) {
      return rpc.getPath(fn);
    };

    RA.getUser = function(fn) {
        return rpc.getUser(fn);
    };

    RA.showMessage = function(id, title, body) {
      rpc.showMessage(id, title, body);
    };

    RA.clearMessage = function(id) {
      rpc.clearMessage(id);
    };

    return RA;
})();