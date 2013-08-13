AP.define("events", ["_dollar", "_rpc"], function ($, rpc) {

  "use strict";

  return rpc.extend(function (remote) {
    // Expose an Events API that delegates the to the underlying XdmRpc events bus; this is necessary since the bus
    // itself isn't actually created until the XdmRpc object is constructed, which hasn't happened yet at this point;
    // see the jsdoc in ../_events.js for API docs
    var apis = {};
    $.each(["listeners", "on", "once", "onAny", "off", "offAll", "offAny", "active", "emit"], function (_, name) {
      apis[name] = function () {
        return remote.events[name].apply(remote.events, arguments);
      };
    });
    return {apis: apis};
  });

});
