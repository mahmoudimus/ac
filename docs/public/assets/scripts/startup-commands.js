/**
 * This script retrieves the atlas-run-standalone commands posted to DAC by the script:
 * /bin/publish-startup-commands.js
 * and adds them to specific spans that must be on the page:
 *  - commands-jira-prd
 *  - commands-jira-dev
 *  - commands-confluence-prd
 *  - commands-confluence-dev
 */

function getCommands() {
    var errorMsgCommand = 'There was an error retrieving the command.'
    var errorMsgVersion = 'Error retrieving the version information'
    $.ajax({
        type: "GET",
        url: '//developer.atlassian.com/static/connect-versions.jsonp',
        dataType: 'jsonp',
        jsonpCallback: "evalCommands"
    }).error(function (data) {
        commands = {
            'environment': {
                'dev': {
                    'connectVersion' : errorMsgVersion,
                    'jiraCommand' : errorMsgCommand,
                    'confluenceCommand' : errorMsgCommand
                },
                'prd': {
                    'connectVersion' : errorMsgVersion,
                    'jiraCommand' : errorMsgCommand,
                    'confluenceCommand' : errorMsgCommand
                }
            }
        };
        renderCommands(commands);
    });
}

function renderCommands(commands) {
    if ($("#commands-jira-prd").length) {
        $("#commands-jira-prd").replaceWith(wrapText(commands.environment.prd.jiraCommand));
    }
    if ($("#commands-confluence-prd").length) {
        $("#commands-confluence-prd").replaceWith(wrapText(commands.environment.prd.confluenceCommand));
    }
    if ($("#commands-jira-dev").length) {
        $("#commands-jira-dev").replaceWith(wrapText(commands.environment.dev.jiraCommand));
    }
    if ($("#commands-confluence-dev").length) {
        $("#commands-confluence-dev").replaceWith(wrapText(commands.environment.dev.confluenceCommand));
    }
}

function wrapText(text) {
    var wrapper = $('<div />').addClass('CodeMirror-wrap').append('<pre />'),
        container = $('<div />').addClass('CodeMirror').css('padding', '4px').text(text);
    wrapper.append(container);
    return wrapper;
}

$(getCommands);


/**
 * JSONP callback
 */
function evalCommands(commands) {
    console.log(commands);
    renderCommands(commands.commands);
}

