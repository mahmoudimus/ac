/**
 * This script retrieves the atlas-run-standalone commands posted to DAC by the script:
 * /bin/publish-startup-commands.js
 * and adds them to specific spans that must be on the page:
 *  - commands-jira-prd
 *  - commands-jira-dev
 *  - commands-confluence-prd
 *  - commands-confluence-dev
 */
var startupCommands = (function() {
    var commands;
    function getCommands() {
        var errorMsgCommand = 'There was an error retrieving the command.';
        var errorMsgVersion = 'There was an error retrieving the version information';
        $.ajax({
            type: "GET",
            url: '//developer.atlassian.com/static/connect/commands/connect-versions.jsonp',
            dataType: 'jsonp',
            jsonpCallback: "evalCommands"
        }).error(function (data) {
            commands = {
                'environment': {
                    'dev': {
                        'connectVersion' : errorMsgVersion,
                        'jiraVersion' : errorMsgVersion,
                        'confluenceVersion' : errorMsgVersion,
                        'jiraCommand' : errorMsgCommand,
                        'confluenceCommand' : errorMsgCommand
                    },
                    'prd': {
                        'connectVersion' : errorMsgVersion,
                        'jiraVersion' : errorMsgVersion,
                        'confluenceVersion' : errorMsgVersion,
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
            $("#commands-jira-prd").replaceWith(wrapText(commands.prd.jiraCommand));
        }
        if ($("#commands-confluence-prd").length) {
            $("#commands-confluence-prd").replaceWith(wrapText(commands.prd.confluenceCommand));
        }
        if ($("#commands-jira-dev").length) {
            $("#commands-jira-dev").replaceWith(wrapText(commands.dev.jiraCommand));
        }
        if ($("#commands-confluence-dev").length) {
            $("#commands-confluence-dev").replaceWith(wrapText(commands.dev.confluenceCommand));
        }
        if($("#connect-version-prd").length) {
            $("#connect-version-prd").replaceWith(commands.prd.connectVersion);
        }
        if($("#connect-version-dev").length) {
            $("#connect-version-dev").replaceWith(commands.dev.connectVersion);
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
    window.evalCommands = function(data) {
        commands = data;
        renderCommands(commands.environment);
    };

    /**
     * Expose a function which can re-run the search for commands to insert.
     *
     * We need the ability to re-run the replacement of commands
     * from within static includes which are also loaded asynchronously.
     *
     * If, when run, the `commands` object is undefined, then we assume the request for the environment json
     * has not yet returned meaning the static includes (which by this time are already in the dom) will
     * be replaced as normal. Otherwise, we use the commands and re-run `renderCommands`.
     */
    return function() {
        if (commands) {
            renderCommands(commands.environment);
        }
    }
})();
