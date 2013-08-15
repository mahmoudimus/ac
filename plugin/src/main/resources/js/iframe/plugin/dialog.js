AP.define("dialog", ["_dollar", "_rpc"], function ($, rpc) {

  "use strict";

  var isDialog = window.location.toString().indexOf("dialog=1") > 0,
      exports;

  rpc.extend(function (remote) {

    // dialog-related sub-api for use when the remote plugin is running as the content of a host dialog

    var listeners = {};

    exports = {

      create: function(options) {
        remote.createDialog(options);
        return {
          on: function (event, callback) {
            remote.events.on("dialog." + event, callback);
          }
        };
      },

      close: function(data) {
        remote.events.emit("dialog.close", data);
        remote.closeDialog();
      },

      isDialog: isDialog,

      // register callbacks responding to messages from the host dialog, such as "submit" or "cancel"
      //
      // @deprecated
      onDialogMessage: function (message, listener) {
        this.getButton(message).bind(listener);
      },

      // gets a button proxy for a button in the host's dialog button panel by name
      getButton: function (name) {
        return {
          name: name,
          enable: function () {
            remote.setDialogButtonEnabled(name, true);
          },
          disable: function () {
            remote.setDialogButtonEnabled(name, false);
          },
          toggle: function () {
            var self = this;
            self.isEnabled(function (enabled) {
              self[enabled ? "disable" : "enable"](name);
            });
          },
          isEnabled: function (callback) {
            remote.isDialogButtonEnabled(name, callback);
          },
          bind: function (listener) {
            var list = listeners[name];
            if (!list) {
              list = listeners[name] = [];
            }
            list.push(listener);
          },
          trigger: function () {
            var self = this,
                cont = true,
                result = true,
                list = listeners[name];
            $.each(list, function (i, listener) {
              result = listener.call(self, {
                button: self,
                stopPropagation: function () { cont = false; }
              });
              return cont;
            });
            return !!result;
          }
        }
      }

    };

    return {

      internals: {

        // forwards dialog event messages from the host application to locally registered handlers
        dialogMessage: function (name) {
          var result = true;
          try {
            if (isDialog) {
              result = exports.getButton(name).trigger();
            }
            else {
              $.handleError("Received unexpected dialog button event from host:", name);
            }
          }
          catch (e) {
            $.handleError(e);
          }
          return result;
        }

      },

      stubs: [
        "setDialogButtonEnabled",
        "isDialogButtonEnabled",
        "createDialog",
        "closeDialog"
      ]

    };

  });

  return exports;

});
