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
                            this.triggerDateSelectedListener(calendarInstance.date.toISOString(), date);
                            if (calendarInstance.dateClicked) {
                                calendarInstance.callCloseHandler();
                            }
                        }.bind(this);

                        var iframeBox = this.iframe.getBoundingClientRect();
                        options.position = options.position || { top: 0, left: 0 };
                        options.position.top += iframeBox.top;
                        options.position.left += iframeBox.left;

                        DatePicker.show(options);
                    }
                },
                stubs: ['triggerDateSelectedListener']
            };
        });
    });

})(AJS.$, require);
