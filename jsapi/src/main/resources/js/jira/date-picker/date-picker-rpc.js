(function($, require){
    "use strict";

    require([
        "ac/jira/date-picker",
        "connect-host"
    ], function(
        DatePicker,
        _AP
    ){
        _AP.extend(function () {
            return {
                internals: {
                    datePicker: function(options) {
                        options.onSelect = function(calendarInstance, date) {
                            this.triggerDateSelectedListener(date, calendarInstance.date.toISOString());
                            calendarInstance.callCloseHandler();
                        }.bind(this);

                        var iframeBox = this.iframe.getBoundingClientRect();
                        options.position = options.position || { top: 0, left: 0 };
                        options.position.top += iframeBox.top;
                        options.position.left += iframeBox.left;

                        DatePicker.create(options);
                    }
                },
                stubs: ['triggerDateSelectedListener']
            };
        });
    });

})(AJS.$, require);
