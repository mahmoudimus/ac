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
                dialogMessage: function(message) {
                    if (dialogHandlers[message] != null)
                    {
                        return dialogHandlers[message]();
                    }
                    return true; // by default, allow the operation to proceed.
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

    var dialogHandlers = {};

    RA.Dialog = {
        onDialogMessage: function(messageName, callback)
        {
            dialogHandlers[messageName] = callback;
        }
    };

    return RA;
})();
