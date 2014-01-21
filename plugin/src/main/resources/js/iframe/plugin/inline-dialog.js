AP.define("inline-dialog", ["_dollar", "_rpc"],

  /**
   * The Inline Dialog module provides a mechanism for launching inline dialogs containing the connect add-on's iframe.
   * A modal inline dialog displays information without requiring the user to leave the current page.
   *
   * For more information, read about the Atlassian User Interface [inline dialog component](https://docs.atlassian.com/aui/latest/docs/inlineDialog.html).
   * @exports inline-dialog
   */

function ($, rpc) {
    "use strict";

    var exports;

    rpc.extend(function (remote) {
        exports = {
            /**
            * Hide the inline dialog that contains your connect add-on.
            * @example
            * AP.require('inline-dialog', function(inlineDialog){
            *   inlineDialog.hide();
            * });
            */
            hide: function () {
                remote.hideInlineDialog();
            }
        }
        return {
            stubs: ['hideInlineDialog']
        }
    });

    return exports;

});
