var RA = (function () {
    var RA = RA || {};
    var rpc;
    RA.init = function (options) {
        rpc = new easyXDM.Rpc({}, {
            remote:{
                resize:{},
                init:{},
                getLocation:{},
                getUser:{},
                showMessage:{},
                clearMessage:{}
            },
            local:{
                onSubmit: function() {
                    console.log("Calling dialog submit handler.");
                    return dialogHandler();
                }
            }
        });
        rpc.init();
        RA.resize();
    };

    RA.resize = function (width, height) {
        var w = width || "100%";
        var h = height || (document.body.offsetHeight + 40);
        rpc.resize(h, w);
    };

    RA.getLocation = function(fn) {
        return rpc.getLocation(fn);
    };

    RA.getUser = function(fn) {
        return rpc.getUser(fn);
    };

    RA.showMessage = function (id, title, body) {
        rpc.showMessage(id, title, body);
    };

    RA.clearMessage = function (id) {
        rpc.clearMessage(id);
    };

    var dialogHandler = function () {
    };

    RA.Dialog = {
        onSubmit:function (callback) {
            dialogHandler = callback;
        }
    };

    return RA;
})();
