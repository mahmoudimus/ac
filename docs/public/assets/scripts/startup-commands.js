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
	    url: '//developer.atlassian.com/static/atlassian-connect-versions.json'
	}).done(function(commands) {
		renderCommands(commands.commands);
	}).error(function(data) {
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
    console.log(commands.commands);
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

function evalCommands(commands) {
    alert(commands);
}

getCommands();