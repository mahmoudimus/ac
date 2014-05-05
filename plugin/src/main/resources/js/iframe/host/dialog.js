_AP.define("dialog", ["_dollar", "_rpc", "dialog/dialog-factory", "dialog/main", "_events"], function ($, rpc, dialogFactory, dialogMain, events) {
    "use strict";

    var opener;


    rpc.extend(function(remote){
        return {
            stubs: ["dialogMessage"],
            init: function(state){
                if(state.uiParams.isDialog){
                    var buttons = dialogMain.getButton();
                    if(buttons){
                        $.each(buttons, function(name, button) {
                            button.click(function (e, callback) {
                                if(state.isActive()){
                                    state.dialogMessage(name, callback);
                                } else {
                                    callback(true);
                                }
                            });
                        });
                    }

                    // on close, do stuff
                }
            },
            local: {

            },
            internals: {
                setDialogButtonEnabled: function (name, enabled) {
                    dialogMain.getButton(name).setEnabled(enabled);
                },
                isDialogButtonEnabled: function (name, callback) {
                    var button =  dialogMain.getButton(name);
                    callback(button ? button.isEnabled() : void 0);
                },
                createDialog: function (dialogOptions) {
                    var xdmOptions = {
                        key: this.addonKey
                    };

                    //open by key or url. This can be simplified when opening via url is removed.
                    if(dialogOptions.key) {
                        xdmOptions.moduleKey = dialogOptions.key;
                    } else if(dialogOptions.url) {
                        xdmOptions.url = dialogOptions.url;
                    }
                    dialogFactory(xdmOptions, dialogOptions, this.productContextJson);
/*
                    this.events.on('dialog.close', function(data){
                        console.log("CREATE DIALOG DIALOG.close", data);
                    });
                    */
                    //this.events.emit("dialog.close");
                    var that = this;
                    opener = function(){
                        return that;
                    };


                },
                closeDialog: function(done, fail) {
                    console.log("ARGS", arguments, this);
                    dialogMain.close('something in close');
                    //opener().events.emit('dialog.close', 'abc');
  /*
                    console.log("CLOSE DIALOG", remote, this, arguments);
                    //opener.dialogMessage("dialog.close", callback);
                    opener().events.emit('dialog.close', 'abc');
                    dialogMain.close('something in close');
                    */
                }
            }
        };
    });

});
