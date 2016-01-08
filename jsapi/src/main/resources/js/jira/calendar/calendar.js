(function($, require){
    "use strict";

    require(["connect-host"], function(_AP){
        _AP.extend(function () {
            return {
                internals: {
                    showCalendar: function(){
                        //alert("calendar shown!!!");
                    }
                }
            };
        });
    });

})(AJS.$, require);
