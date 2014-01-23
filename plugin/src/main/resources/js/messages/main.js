_AP.define("messages/main", ["_dollar"], function($) {
	var MESSAGE_BAR_SELECTOR = 'aui-message-bar',
		MESSAGE_TYPES = ["generic", "error", "warning", "success", "info", "hint"];

	function validateMessageId(msgId){
		return msgId.search(/^ap\-message\-[0-9]+$/) == 0;
	}

	//put the bar in a good place depending on the page.
	function createMessageBar(){
		//generalPage
		$('<div id="' + MESSAGE_BAR_SELECTOR + '" />').appendTo('#main-header');

	}
	function filterMessageOptions(options){
		//UNDERSCORE RELIANCE!!!
		return _.pick(options, 'closeable', 'fadeout', 'delay', 'duration', 'id');
	}

	return {
        showMessage: function (name, title, body, options) {
        	if($('#' + MESSAGE_BAR_SELECTOR).length < 1){
        		createMessageBar();
        	}
        	options = filterMessageOptions(options);
        	$.extend(options, {title: title, body: body });

        	if($.inArray(name, MESSAGE_TYPES) < 0){
        		throw "Invalid message type";
        	}
        	if(validateMessageId(options.id)){
        		AJS.messages[name](options);
        	}
        },
        clearMessage: function () {
            console.log('in server side of clearMessage');
        }
    }
});
