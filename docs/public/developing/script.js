
function getCommands() {
	$.ajax({
		type: "GET",
	    url: "https://connect-utils.firebaseio.com/commands.json",
	}).done(function(commands) {
		renderCommands(commands);
	}).error(function(data) {
		console.log('error loading commands: ' + data);
		commands = {
			'jira': {
				'dev':'There was an error retrieving the command.',
				'prd':'There was an error retrieving the command.'
			},
			'confluence': {
				'dev':'There was an error retrieving the command.',
				'prd':'There was an error retrieving the command.'
			}
				};
		renderCommands(commands);
	});

}

function wrapText(text){
	var	wrapper = $('<div />').addClass('CodeMirror-wrap').append('<pre />'),
		container = $('<div />').addClass('CodeMirror').css('padding', '4px').text(text);
	wrapper.append(container);
	return wrapper;
}

function renderCommands(commands) {
	$("#commands-jira-prd").replaceWith(wrapText(commands.jira.prd));
	$("#commands-jira-dev").replaceWith(wrapText(commands.jira.dev));
	$("#commands-confluence-prd").replaceWith(wrapText(commands.confluence.prd));
	$("#commands-confluence-dev").replaceWith(wrapText(commands.confluence.dev));
}

getCommands();