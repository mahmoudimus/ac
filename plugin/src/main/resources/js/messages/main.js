_AP.define("messages/main", ["_dollar"], function($) {
	var MESSAGE_BAR_SELECTOR = 'aui-message-bar',
		MESSAGE_TYPES = ["generic", "error", "warning", "success", "info", "hint"];

	var messageId = 0;

	function getMessageId(){
		messageId++;
		return 'ap-message-' + messageId;
	}

	//put the bar in a good place depending on the page.
	function createMessageBar(){
		//generalPage
		$('<div id="' + MESSAGE_BAR_SELECTOR + '" />').appendTo('#main-header');

	}
	function filterMessageOptions(options){
		//UNDERSCORE RELIANCE!!!
		return _.pick(options, 'closeable', 'fadeout', 'delay', 'duration');
	}
    return {
        showMessage: function (name, title, body, options) {
        	if($('#' + MESSAGE_BAR_SELECTOR).length < 1){
        		createMessageBar();
        	}
        	var opts = (options) ? filterMessageOptions(options) : {};

        	$.extend(opts, {title: title, body: body, id: getMessageId()});

        	if($.inArray(name, MESSAGE_TYPES) < 0){
        		throw "Invalid message type";
        	}

			AJS.messages[name](opts);
			console.log(opts.id);
			return opts.id;
        },
        clearMessage: function () {
            console.log('in server side of clearMessage');
        }
    }
});
