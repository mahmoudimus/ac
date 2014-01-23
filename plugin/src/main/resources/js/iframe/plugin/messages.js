AP.define("messages", ["_dollar", "_rpc"],

/**
* @exports messages
* @see https://docs.atlassian.com/aui/5.4.1/docs/messages.html
*/

function ($, rpc) {
    "use strict";

    return rpc.extend(function (remote) {

        var apis = {};
        $.each(["generic", "error", "warning", "success", "info", "hint"], function (_, name) {
            /**
            * @param    {String}            title       Sets the title text of the message.
            * @param    {String}            body        The main content of the message.
            * @param    {MessageOptions}    options     Message Options
            * @returns  {String}    The id to be used when clearing the message
            */
            apis[name] = function (title, body, options) {
                return remote.showMessage(name, title, body, options);
            };
        });

        apis.clear = function(id){
            return remote.clearMessage(id);
        }

        return {
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
