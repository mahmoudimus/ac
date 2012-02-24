var RA = (function() {
    var RA = RA || {};
    var rpc;
    RA.init = function(options) {
        rpc = new easyXDM.Rpc({}, {
            remote : {
                resize : {},
                init : {},
                getLocation : {}
            }
        });
        rpc.init();
    };

    RA.resize = function(width, height) {
        var w = width || "100%";
        var h = height || (document.body.offsetHeight + 40);
        rpc.resize(h, w);
    };

    RA.getLocation = function() {
        return rpc.getLocation();
    };

    return RA;
})();


