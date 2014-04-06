_AP.define("messages/main", ["_dollar"], function($) {
    var MESSAGE_BAR_ID = 'ac-message-container',
        MESSAGE_TYPES = ["generic", "error", "warning", "success", "info", "hint"];

    function validateMessageId(msgId){
        return msgId.search(/^ap\-message\-[0-9]+$/) == 0;
    }

    function getMessageBar(){
        var msgBar = $('#' + MESSAGE_BAR_ID);

        if(msgBar.length < 1){
            msgBar = $('<div id="' + MESSAGE_BAR_ID + '" />').appendTo('body');
        }
        return msgBar;
    }

    function filterMessageOptions(options){
        var i,
        key,
        copy = {},
        allowed = ['closeable', 'fadeout', 'delay', 'duration', 'id'];

        for (i in allowed){
            key = allowed[i];
            if (key in options){
                copy[key] = options[key];
            }
        }

        return copy;
    }

    return {
        showMessage: function (name, title, body, options) {
            var msgBar = getMessageBar();

            options = filterMessageOptions(options);
            body = $('<div />').text(body).html(); // encode html entities
            $.extend(options, {title: title, body: body});

            if($.inArray(name, MESSAGE_TYPES) < 0){
                throw "Invalid message type. Must be: " + MESSAGE_TYPES.join(", ");
            }
            if(validateMessageId(options.id)){
                AJS.messages[name](msgBar, options);
                // Calculate the left offset based on the content width.
                // This ensures the message always stays in the centre of the window.
                msgBar.css('margin-left', '-' + msgBar.innerWidth()/2 + 'px');
            }
        },
        clearMessage: function (id) {
            if(validateMessageId(id)){
                $('#' + id).remove();
            }
        }
    }
});
