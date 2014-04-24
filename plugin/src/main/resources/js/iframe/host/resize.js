_AP.define("resize", ["_dollar", "_rpc"], function ($, rpc) {
    "use strict";
    // a simplified version of underscore's debounce
    function debounce(fn, wait) {
      var timeout;
      return function() {
        var ctx = this,
            args = [].slice.call(arguments);
        function later() {
          timeout = null;
          fn.apply(ctx, args);
        }
        if (timeout) {
          clearTimeout(timeout);
        }
        timeout = setTimeout(later, wait || 50);
      };
    }

    rpc.extend(function(config){
      return {
        internals: {
          resize: debounce(function(width, height){
            $(config.iframe).css({width: width, height: height});
          })
        }
      };
    });

});
