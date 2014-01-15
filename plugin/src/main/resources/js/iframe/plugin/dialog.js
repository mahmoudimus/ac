AP.define("dialog", ["_dollar", "_rpc"],

  /**
   * This allows you to open and manage dialogs from inside your javascript. This is helpful in instances where you need to open a dialog when a user interacts with your add-on.
   *
   * @exports Dialog
   */

  function ($, rpc) {
  "use strict";

  var isDialog = window.location.toString().indexOf("dialog=1") > 0,
      exports;

  rpc.extend(function (remote) {

    // dialog-related sub-api for use when the remote plugin is running as the content of a host dialog

    var listeners = {};

    exports = {
      /**
      * Creates a dialog for a module key
      * @example
      * AP.require('dialog', function(dialog){
      *   dialog.create('mydialog');
      * });
      */
      create: function(options) {
        remote.createDialog(options);
        return {
          on: function (event, callback) {
            // HACK: Note this is a "once" as it's assumed the only event is "close", and close is only fired
            // once per dialog. If we changed this to "on", then it would be fired when *any* dialog is closed,
            // meaning that if say two dialog were opened, closed, opened, then closed, then the callback
            // registered for the first dialog would be issued when the second was closed.
            remote.events.once("dialog." + event, callback);
          }
        };
      },
      /**
      * Closes the currently open dialog. Optionally pass data to listeners of the dialog.close event.
      * @param {Object} data to be emitted on dialog close.
      * @example
      * AP.require('dialog', function(dialog){
      *   dialog.close({foo: 'bar'});
      * });
      */
      close: function(data) {
        remote.events.emit("dialog.close", data);
        remote.closeDialog();
      },

      isDialog: isDialog,

      /**
      * register callbacks responding to messages from the host dialog, such as "submit" or "cancel"
      * @deprecated
      */
      onDialogMessage: function (message, listener) {
        this.getButton(message).bind(listener);
      },
      /**
      * Returns the button that was requested (either cancel or submit)
      * @returns {DialogButton} Returns the button
      * @example
      * AP.require('dialog', function(dialog){
      *   dialog.getButton('submit');
      * });
      */
      getButton: function (name) {
        /**
        * @class DialogButton
        */
        return {
          name: name,

          /**
          * Sets the button to enabled
          * @memberOf DialogButton
          * @example
          * AP.require('dialog', function(dialog){
          *   dialog.getButton('submit').enable();
          * });
          */
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
