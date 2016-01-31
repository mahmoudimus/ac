define("ac/jira/date-picker", [
    "underscore"
], function(
    _
){
    var config = WRM.data.claim("com.atlassian.plugins.atlassian-connect-plugin:jira-date-picker-widget.config");

    function show(params) {
        var cal = window.calendar;
        if (cal) {
            cal.hide();
        }

        params = params || {};
        params = _.extend(params, config, {
            singleClick: "true",
            inputField: {}
        });

        // Exposing this as a global variable in order for JIRA to recognize it properly as its calendar control.
        // This way it will be able to hide it when showing other calendars (e.g. for the date picker).
        window.calendar = cal = new Calendar(
            // Either use ISO week numbers or pass undefined so the component can decide based on locale
            // ISO8601 assumes that first day of week is Monday
            (params.useISO8601WeekNumbers) ? 1 : undefined,
            params.date,
            new Date().toISOString(),
            params.onSelect || function noop() {  },
            function hideAndDestroy(cal) {
                cal.hide();
                cal.destroy();
            }
        );

        cal.weekNumbers = true;
        cal.showsOtherMonths = false;
        cal.params = params;
        cal.showsTime = params.showTime;
        cal.time24 = (params.timeFormat == "24");
        var formatString = params.showTime ? params.dateTimeFormat : params.dateFormat;
        cal.setDateFormat(formatString);

        cal.create();
        cal.refresh();
        cal.showAt(params.position.left || 0, params.position.top || 0);

        return cal;
    }

    return {
        show: show
    }
});

