/**
 * This script retrieves the atlas-run-standalone commands posted to Firebase by the script:
 * /bin/publish-startup-commands.js
 * and adds them to specific spans that must be on the page:
 *  - commands-jira-prd
 *  - commands-jira-dev
 *  - commands-confluence-prd
 *  - commands-confluence-dev
 */

function getCommands() {
	$.ajax({
		type: "GET",
	    url: "https://connect-utils.firebaseio.com/commands.json"
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

function renderCommands(commands) {
    if($("#commands-jira-prd")) {
        $("#commands-jira-prd").replaceWith(wrapText(commands.jira.prd));
    }
    if($("#commands-confluence-prd")) {
        $("#commands-confluence-prd").replaceWith(wrapText(commands.confluence.prd));
    }
    if($("#commands-jira-dev")) {
        $("#commands-jira-dev").replaceWith(wrapText(commands.jira.dev));
    }
    if($("#commands-confluence-dev")) {
        $("#commands-confluence-dev").replaceWith(wrapText(commands.confluence.dev));
    }
}

function wrapText(text){
    var	wrapper = $('<div />').addClass('CodeMirror-wrap').append('<pre />'),
        container = $('<div />').addClass('CodeMirror').css('padding', '4px').text(text);
    wrapper.append(container);
    return wrapper;
}

getCommands();