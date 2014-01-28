AP.define("events", ["_dollar", "_rpc"], 
    /**
    * The Events module provides a mechanism for emitting and receiving events.
    * <h3>Basic example</h3>
    * ```
    * //The following will create an alert message every time the event `customEvent` is triggered.
    * AP.require('events', function(events){
    *   events.on('customEvent', function(){
    *       alert('event fired');
    *   });
    *   events.emit('customEvent');
    * });
    * ```
    * @name Events
    * @module
    */

    function ($, rpc) {

    "use strict";

    return rpc.extend(function (remote) {
    // Expose an Events API that delegates the to the underlying XdmRpc events bus; this is necessary since the bus
    // itself isn't actually created until the XdmRpc object is constructed, which hasn't happened yet at this point;
    // see the jsdoc in ../_events.js for API docs
    var apis = {};
    $.each(["on", "once", "onAny", "off", "offAll", "offAny", "emit"], function (_, name) {
      apis[name] = function () {
        var events = remote.events;
        events[name].apply(events, arguments);
        return apis;
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
//      var events = remote.events;
//      var event = events._event.apply(events, arguments);
//      event.attrs._acAllow = regex.toString();
//      events._emitEvent(event);
//      return apis;
//    };

    return {
     /**
     * Adds a listener for all occurrences of an event of a particular name.
     * Listener arguments include any arguments passed to `events.emit`, followed by an object describing the complete event information.
     * @name on
     * @method
     * @memberof module:Events#
     * @param {String} name The event name to subscribe the listener to
     * @param {Function} listener A listener callback to subscribe to the event name
     */

    /**
     * Adds a listener for one occurrence of an event of a particular name.
     * Listener arguments include any argument passed to `events.emit`, followed by an object describing the complete event information.
     * @name once
     * @method
     * @memberof module:Events#
     * @param {String} name The event name to subscribe the listener to
     * @param {Function}listener A listener callback to subscribe to the event name
     */

    /**
     * Adds a listener for all occurrences of any event, regardless of name.
     * Listener arguments begin with the event name, followed by any arguments passed to `events.emit`, followed by an object describing the complete event information.
     * @name onAny
     * @method
     * @memberof module:Events#
     * @param {Function} listener A listener callback to subscribe for any event name
     */

    /**
     * Removes a particular listener for an event.
     * @name off
     * @method
     * @memberof module:Events#
     * @param {String} name The event name to unsubscribe the listener from
     * @param {Function} listener The listener callback to unsubscribe from the event name
     */

    /**
     * Removes all listeners from an event name, or unsubscribes all event-name-specific listeners
     * if no name if given.
     * @name offAll
     * @method
     * @memberof module:Events#
     * @param {String} [name] The event name to unsubscribe all listeners from
     */

    /**
     * Removes an `any` event listener.
     * @name offAny
     * @method
     * @memberof module:Events#
     * @param {Function} listener A listener callback to unsubscribe from any event name
     */

    /**
     * Emits an event on this bus, firing listeners by name as well as all 'any' listeners. Arguments following the
     * name parameter are captured and passed to listeners.
     * @name emit
     * @method
     * @memberof module:Events#
     * @param {String} name The name of event to emit
     * @param {String[]} args 0 or more additional data arguments to deliver with the event
     */
      apis: apis
    };
  });

});
