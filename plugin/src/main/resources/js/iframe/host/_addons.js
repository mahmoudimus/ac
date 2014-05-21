(window.AP || window._AP).define("host/_addons", ["_dollar", "_rpc"], function ($, rpc) {

  "use strict";

  // Note that if it's desireable to publish host-level events to add-ons, this would be a good place to wire
  // up host listeners and publish to each add-on, rather than using each XdmRpc.events object directly.

    var _channels = {};

  // Tracks all channels (iframes with an XDM bridge) for a given add-on key, managing event propagation
  // between bridges, and potentially between add-ons.

    rpc.extend(function () {

        var self = {
            _emitEvent: function (event) {
                $.each(_channels[event.source.key], function (id, channel) {
                    channel.bus._emitEvent(event);
                });
            },
            remove: function (xdm) {
                var channel = _channels[xdm.addonKey][xdm.id];
                if (channel) {
                    channel.bus.offAny(channel.listener);
                }
                delete _channels[xdm.addonKey][xdm.id];
                return this;
            },
            init: function (config, xdm) {
                if(!_channels[xdm.addonKey]){
                    _channels[xdm.addonKey] = {};
                }
                var channel = _channels[xdm.addonKey][xdm.id] = {
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

});
