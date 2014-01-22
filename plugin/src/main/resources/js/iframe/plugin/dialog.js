AP.define("dialog", ["_dollar", "_rpc"],

  /**
   * The Dialog module provides a mechanism for launching modal dialogs from within an add-on's iframe.
   * A modal dialog displays information without requiring the user to leave the current page.
   * The dialog is opened over the entire window, rather than within the iframe itself.
   *
   * For more information, read about the Atlassian User Interface [dialog component](https://docs.atlassian.com/aui/latest/docs/dialog.html).
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
      * @param {DialogOptions} options configuration object of dialog options.
      * @example
      * AP.require('dialog', function(dialog){
      *   dialog.create('mydialog');
      * });
      */
      create: function(options) {
        /**
        * @name DialogOptions
        * @class
        * @property {String} key The module key of the page you want to open as a dialog
        * @property {String} size Opens the dialog at a preset size: small, medium, large, x-large or maximum (full screen).
        * @property {Number|String} width instead of size, define the width as a percentage (append a % to the number) or pixels.
        * @property {Number|String} height instead of size, define the height as a percentage (append a % to the number) or pixels.
        */
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
      * Closes the currently open dialog. Optionally pass data to listeners of the `dialog.close` event.
      * This will only close a dialog that has been opened by your add-on.
      * You can register for close events using the `dialog.close` event and the [events module](module-Event.html)
      * @param {Object} data An object to be emitted on dialog close.
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
      * @param String button either "cancel" or "submit"
      * @param Function callback function
      * @deprecated
      */
      onDialogMessage: function (message, listener) {
        this.getButton(message).bind(listener);
      },
      /**
      * Returns the button that was requested (either cancel or submit)
      * @returns {DialogButton}
      * @example
      * AP.require('dialog', function(dialog){
      *   dialog.getButton('submit');
      * });
      */
      getButton: function (name) {
        /**
        * @class DialogButton
        * @description A dialog button that can be controlled with javascript
        */
        return {
          name: name,

          /**
          * Sets the button state to enabled
          * @memberOf DialogButton
          * @example
          * AP.require('dialog', function(dialog){
          *   dialog.getButton('submit').enable();
          * });
          */
          enable: function () {
            remote.setDialogButtonEnabled(name, true);
          },
          /**
          * Sets the button state to disabled
          * @memberOf DialogButton
          * @example
          * AP.require('dialog', function(dialog){
          *   dialog.getButton('submit').disable();
          * });
          */
          disable: function () {
            remote.setDialogButtonEnabled(name, false);
          },
          /**
          * Toggle the button state between enabled and disabled.
          * @memberOf DialogButton
          * @example
          * AP.require('dialog', function(dialog){
          *   dialog.getButton('submit').toggle();
          * });
          */
          toggle: function () {
            var self = this;
            self.isEnabled(function (enabled) {
              self[enabled ? "disable" : "enable"](name);
            });
          },
          /**
          * Query a button for it's current state.
          * @memberOf DialogButton
          * @param {Function} callback function to receive the button state.
          * @example
          * AP.require('dialog', function(dialog){
          *   dialog.getButton('submit').isEnabled(function(enabled){
          *     if(enabled){
          *       //button is enabled
          *     }
          *   });
          * });
          */
          isEnabled: function (callback) {
            remote.isDialogButtonEnabled(name, callback);
          },
          /**
          * Registers a function to be called when the button is clicked.
          * @memberOf DialogButton
          * @param {Function} callback function to be triggered on click or programatically.
          * @example
          * AP.require('dialog', function(dialog){
          *   dialog.getButton('submit').bind(function(){
          *     alert('clicked!');
          *   });
          * });
          */
          bind: function (listener) {
            var list = listeners[name];
            if (!list) {
              list = listeners[name] = [];
            }
            list.push(listener);
          },
          /**
          * Trigger a callback bound to a button.
          * @memberOf DialogButton
          * @example
          * AP.require('dialog', function(dialog){
          *   dialog.getButton('submit').bind(function(){
          *     alert('clicked!');
          *   });
          *   dialog.getButton('submit').trigger();
          * });
          */
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
