(window.AP || window._AP).define("host/_addons", ["_dollar", "_rpc"], function ($, rpc) {

  "use strict";

  // Note that if it's desireable to publish host-level events to add-ons, this would be a good place to wire
  // up host listeners and publish to each add-on, rather than using each XdmRpc.events object directly.

    var addons = {},
    _channels = {};

  // Tracks all channels (iframes with an XDM bridge) for a given add-on key, managing event propagation
  // between bridges, and potentially between add-ons.

    rpc.extend(function () {

        var self = {
            _emitEvent: function(event){
                $.each(_channels, function (id, channel) {
                    channel.bus._emitEvent(event);
                });
            },
            remove: function (xdm) {
                var channel = _channels[xdm.id];
                if (channel) {
                    channel.bus.offAny(channel.listener);
                }
                delete _channels[xdm.id];
                return this;
            },
            init: function(config, xdm){
                var channel = _channels[xdm.id] = {
                    bus: xdm.events,
                    listener: function () {
                        var event = arguments[arguments.length - 1];
                        var trace = event.trace = event.trace || {};
                        var traceKey = xdm.id + "|addon";
                        if (!trace[traceKey]) {
                            // Only forward an event once in this listener
                            trace[traceKey] = true;
                            self._emitEvent(event);
                        }
                    }

// TODO: Experimental cross-addon eventing
//    // Try to forward to other add-ons if the whitelist regex is specified on the event object
//    if (event.attrs._acAllow) {
//      // Deserialize the allowed add-on key whitelisting regex
//      var re = RegExp.apply(null, /^\/(.*)\/(.*)/.exec(event.attrs._acAllow).slice(1, 3));
//      // Forward the event to other add-ons matching the whitelist regex
//      $.each(addons, function (key, addon) {
//        if (event.source.key !== key && re.test(key)) {
//          addon._emitEvent(event);
//        }
//      });
//    }

                };
                channel.bus.onAny(channel.listener); //forward add-on events.

                // Remove reference to destroyed iframes such as closed dialogs.
                channel.bus.on("ra.iframe.destroy", function(){
                    self.remove(xdm);
                }); 
            }
        };
        return self;
    });
/*
  function Addon(key) {
    this.key = key;
    this._channels = {};
  }

  var proto = Addon.prototype;

  proto.add = function (xdm) {
    var self = this;
    var channel = self._channels[xdm.id] = {
      bus: xdm.events,
      listener: function () {
        var event = arguments[arguments.length - 1];
        var trace = event.trace = event.trace || {};
        var traceKey = xdm.id + "|addon";
        if (!trace[traceKey]) {
          // Only forward an event once in this listener
          trace[traceKey] = true;
          self._emitEvent(event);
        }
// TODO: Experimental cross-addon eventing
//    // Try to forward to other add-ons if the whitelist regex is specified on the event object
//    if (event.attrs._acAllow) {
//      // Deserialize the allowed add-on key whitelisting regex
//      var re = RegExp.apply(null, /^\/(.*)\/(.*)/.exec(event.attrs._acAllow).slice(1, 3));
//      // Forward the event to other add-ons matching the whitelist regex
//      $.each(addons, function (key, addon) {
//        if (event.source.key !== key && re.test(key)) {
//          addon._emitEvent(event);
//        }
//      });
//    }
      }
    };
    channel.bus.onAny(channel.listener);
    return this;
  };

  proto.remove = function (xdm) {
    var channel = this._channels[xdm.id];
    if (channel) {
      channel.bus.offAny(channel.listener);
    }
    delete this._channels[xdm.id];
    return this;
  };

  proto._emitEvent = function (event) {
    $.each(this._channels, function (id, channel) {
      channel.bus._emitEvent(event);
    });
  };

  return {

    get: function (key) {
      return addons[key] = addons[key] || new Addon(key);
    }

  };
*/
});
