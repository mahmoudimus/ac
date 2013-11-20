AP.define("inline-dialog", ["_dollar", "_rpc"], function ($, rpc) {

    "use strict";

    var onShowCallback,
        onHideCallback;

    return rpc.extend(function (remote) {

        return {
            apis: {
                onHide: function (callback) {
                    onHideCallback = callback;
                },
                onShow: function (callback) {
                    onShowCallback = callback;
                },
                hideInlineDialog: function (){
                    var valid = true;
                    if(onHideCallback){
                        valid = onHideCallback.call();
                    }
                    if(valid){
                        remote.hideInlineDialog();
                    }
                }
            },
        };
    });

});
