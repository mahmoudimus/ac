define("ac/jira/date-picker", [
    "underscore"
], function(
    _
){
    var config = WRM.data.claim("com.atlassian.plugins.atlassian-connect-plugin:jira-date-picker-widget.config");

    function create(params) {
        params = params || {};
        params.inputField = {};
        params = _.extend(params, config, {
            singleClick: "true"
        });

        var cal = window.calendar;
        if (cal) {
            cal.hide();
        }

        window.calendar = cal = new Calendar(
            params.firstDay,
            params.date,
            params.onSelect || function noop() {  },
            params.onClose || function hideAndDestroy(cal) {
                cal.hide();
                cal.destroy();
            }
        );

        cal.weekNumbers = params.weekNumbers || true;
        // BB - At the Date object level not Calendar
        Date.useISO8601WeekNumbers = params.useISO8601WeekNumbers;
        if (params.useISO8601WeekNumbers) {
            // ISO8601 assumes that first day of week is Monday
            cal.firstDayOfWeek = 1;
        }

        cal.showsOtherMonths = params.showOthers;
        if (Array.isArray(params.range)) {
            cal.setRange(params.range[0], params.range[1]);
        }
        cal.params = params;
        cal.setDateStatusHandler(params.dateStatusFunc);
        cal.getDateText = params.dateText;


        var formatString = params.showsTime ? params.dateTimeFormat : params.dateFormat;
        cal.showsTime = params.showsTime;
        cal.time24 = (params.timeFormat == "24");
        cal.setDateFormat(formatString);

        cal.create();
        cal.refresh();

        cal.showAt(params.position.left, params.position.top);

        return cal;
    }

    return {
        create: create
    }
});

