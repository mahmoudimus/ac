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

// TODO: Experimental cross-addon eventing
//    // Add additional methods that tag the event as being globally-distributable to all addons.
//
//    apis.emitGlobal = function (name) {
//      return apis.emitWhitelist.apply(apis, [/.*/].concat([].slice.call(arguments, 1)));
//    };
//
//    apis.emitWhitelist = function (regex, name) {
//      remote.events._event.apply(remote.events, arguments).attrs({
//        _acAllow: regex.toString()
//      }).emit();
//      return remote.events;
//    };

    return {
      apis: apis
    };
  });

});
