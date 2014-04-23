/**
 * Methods for showing the status of a connect-addon (loading, time'd-out etc)
 */

_AP.define("host/_status_helper", ["_dollar"], function ($) {
    "use strict";

    var statuses = {
        loading: {
            descriptionHtml: '<div class="small-spinner"></div>Loading add-on...'
        },
        "load-timeout": {
            descriptionHtml: '<div class="small-spinner"></div>Add-on is not responding. Wait or <a href="#" class="ap-btn-cancel">cancel</a>?'
        },

        "load-error": {
            descriptionHtml: 'Add-on failed to load.'
        }
    };

    function hideStatuses($home){
        $home.find(".ap-status").addClass("hidden");
    }

    function showStatus($home, status){
        hideStatuses($home);
        $home.find('.ap-' + status).removeClass('hidden');
        /* setTimout fixes bug in AUI spinner positioning */
        setTimeout(function(){
            var spinner = AJS.$('.small-spinner', '.ap-' + status);
            if(spinner.length){
                spinner.spin({zIndex: "1"});
            }
        }, 10);
    }

    //when an addon has loaded. Hide the status bar.
    function showLoadedStatus($home){
        hideStatuses($home);
    }

    function showLoadingStatus($home){
        showStatus($home, 'loading');
    }

    function showloadTimeoutStatus($home){
        showStatus($home, 'load-timeout');
    }

    function showLoadErrorStatus($home){
        showStatus($home, 'load-error');
    }

    function createStatusMessages() {
        var i,
        stats = $('<div class="ap-stats" />');

        for(i in statuses){
            var status = $('<div class="ap-' + i + ' ap-status hidden" />');
            status.append('<small>' + statuses[i].descriptionHtml + '</small>');
            stats.append(status);
        }
        return stats;
    }

    return {
        createStatusMessages: createStatusMessages,
        showLoadingStatus: showLoadingStatus,
        showloadTimeoutStatus: showloadTimeoutStatus,
        showLoadErrorStatus: showLoadErrorStatus,
        showLoadedStatus: showLoadedStatus
    }

});
