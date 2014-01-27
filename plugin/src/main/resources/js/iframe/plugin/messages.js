AP.define("messages", ["_dollar", "_rpc"],

/**
* Messages are the primary method for providing system feedback in the product user interface.
* Messages include notifications of various kinds: alerts, confirmations, notices, warnings, info and errors.
* For visual examples of each kind please see the [Design guide](https://developer.atlassian.com/design/latest/communicators/messages/).
* ### Example ###
* ```
* AP.require("messages", function(messages){
*   //create a message
*   var message = messages.info('plain text title', 'plain text body');
* });
* ```
* @exports messages
*/

function ($, rpc) {
    "use strict";

    var messageId = 0;

    function getMessageId(){
        messageId++;
        return 'ap-message-' + messageId;
    }

    return rpc.extend(function (remote) {

        var apis = {};
        $.each(["generic", "error", "warning", "success", "info", "hint"], function (_, name) {
            apis[name] = function (title, body, options) {
                options = options || {};
                options.id = getMessageId();
                remote.showMessage(name, title, body, options);
                return options.id;
            };
        });

        /**
        * clear a message
        * @name clear
        * @method
        * @memberof module:messages#
        * @param    {String}    id  The id that was returned when the message was created.
        * @example
        * AP.require("messages", function(messages){
        *   //create a message
        *   var message = messages.info('title', 'body');
        *   messages.clear(message);
        * });
        */

        apis.clear = function(id){
            remote.clearMessage(id);
        }

        return {
            /**
            * Show a generic message
            * @name generic
            * @method
            * @memberof module:messages#
            * @param    {String}            title       Sets the title text of the message.
            * @param    {String}            body        The main content of the message.
            * @param    {MessageOptions}    options     Message Options
            * @returns  {String}    The id to be used when clearing the message
            */

            /**
            * Show an error message
            * @name error
            * @method
            * @memberof module:messages#
            * @param    {String}            title       Sets the title text of the message.
            * @param    {String}            body        The main content of the message.
            * @param    {MessageOptions}    options     Message Options
            * @returns  {String}    The id to be used when clearing the message
            */

            /**
            * Show a warning message
            * @name warning
            * @method
            * @memberof module:messages#
            * @param    {String}            title       Sets the title text of the message.
            * @param    {String}            body        The main content of the message.
            * @param    {MessageOptions}    options     Message Options
            * @returns  {String}    The id to be used when clearing the message
            */

            /**
            * Show a success message
            * @name success
            * @method
            * @memberof module:messages#
            * @param    {String}            title       Sets the title text of the message.
            * @param    {String}            body        The main content of the message.
            * @param    {MessageOptions}    options     Message Options
            * @returns  {String}    The id to be used when clearing the message
            */

            /**
            * Show an info message
            * @name info
            * @method
            * @memberof module:messages#
            * @param    {String}            title       Sets the title text of the message.
            * @param    {String}            body        The main content of the message.
            * @param    {MessageOptions}    options     Message Options
            * @returns  {String}    The id to be used when clearing the message
            */

            /**
            * Show a hint message
            * @name hint
            * @method
            * @memberof module:messages#
            * @param    {String}            title       Sets the title text of the message.
            * @param    {String}            body        The main content of the message.
            * @param    {MessageOptions}    options     Message Options
            * @returns  {String}    The id to be used when clearing the message
            */

            apis: apis,
            stubs: ['showMessage', 'clearMessage']
        };
    });

});

/**
* @name MessageOptions
* @class
* @property {Boolean}   closeable   Adds a control allowing the user to close the message, removing it from the page.
* @property {Boolean}   fadeout     Toggles the fade away on the message
* @property {Number}    delay       Time to wait (in ms) before starting fadeout animation (ignored if fadeout==false)
* @property {Number}    duration    Fadeout animation duration in milliseconds (ignored if fadeout==false)
*/
