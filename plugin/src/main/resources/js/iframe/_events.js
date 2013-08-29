(window.AP || window._AP).define("_events", ["_dollar"], function ($) {

  "use strict";

  var w = window,
      log = $.log || (w.AJS && w.AJS.log);

  /**
   * A simple pub/sub event bus capable of running on either side of the XDM bridge with no external
   * JS lib dependencies.
   *
   * @param {String} key The key of the event source
   * @param {String} origin The origin of the event source
   * @constructor
   */
  function Events(key, origin) {
    this._key = key;
    this._origin = origin;
    this._events = {};
    this._any = [];
  }

  var proto = Events.prototype;

  /**
   * Returns an array of listeners by event name.
   *
   * @param {String} name The event name for which listeners should be returned
   * @returns {Array} An array of listeners, or an empty array if none are registered for the given name
   */
  proto.listeners = function (name) {
    return [].slice.call(this._events[name] || []);
  };

  /**
   * Subscribes a callback to an event name.
   *
   * @param {String} name The event name to subscribe the listener to
   * @param {Function} listener A listener callback to subscribe to the event name
   * @returns {Events} This Events instance
   */
  proto.on = function (name, listener) {
    if (name && listener) {
      this._listeners(name).push(listener);
    }
    return this;
  };

  /**
   * Subscribes a callback to an event name, removing the it once fired.
   *
   * @param {String} name The event name to subscribe the listener to
   * @param {Function}listener A listener callback to subscribe to the event name
   * @returns {Events} This Events instance
   */
  proto.once = function (name, listener) {
    var self = this;
    var interceptor = function () {
      self.off(name, interceptor);
      listener.apply(null, arguments);
    };
    this.on(name, interceptor);
    return this;
  };

  /**
   * Subscribes a callback to all events, regardless of name.
   *
   * @param {Function} listener A listener callback to subscribe for any event name
   * @returns {Events} This Events instance
   */
  proto.onAny = function (listener) {
    this._any.push(listener);
    return this;
  };

  /**
   * Unsubscribes a callback to an event name.
   *
   * @param {String} name The event name to unsubscribe the listener from
   * @param {Function} listener The listener callback to unsubscribe from the event name
   * @returns {Events} This Events instance
   */
  proto.off = function (name, listener) {
    var all = this._events[name];
    if (all) {
      var i = all.indexOf(listener);
      if (i >= 0) {
        all.splice(i, 1);
      }
      if (all.length === 0) {
        delete this._events[name];
      }
    }
    return this;
  };

  /**
   * Unsubscribes all callbacks from an event name, or unsubscribes all event-name-specific listeners
   * if no name if given.
   *
   * @param {String} [name] The event name to unsubscribe all listeners from
   * @returns {Events} This Events instance
   */
  proto.offAll = function (name) {
    if (name) {
      delete this._events[name];
    } else {
      this._events = {};
    }
    return this;
  };

  /**
   * Unsubscribes a callback from the set of 'any' event listeners.
   *
   * @param {Function} listener A listener callback to unsubscribe from any event name
   * @returns {Events} This Events instance
   */
  proto.offAny = function (listener) {
    var any = this._any;
    var i = any.indexOf(listener);
    if (i >= 0) {
      any.splice(i, 1);
    }
    return this;
  };

  /**
   * Returns an array containing all event names with listeners.
   *
   * @returns {Array} All active event names, or an empty array
   */
  proto.active = function () {
    // Not using $.map here since it's not implemented in the $-shim on the iframe side
    var names = [];
    $.each(this._events, function (k, v) {
      if (v && v.length > 0) names.push(k);
    });
    return names;
  };

  /**
   * Emits an event on this bus, firing listeners by name as well as all 'any' listeners. Arguments following the
   * name parameter are captured and passed to listeners.  The last argument received by all listeners after the
   * unpacked arguments array will be the fired event object itself, which can be useful for reacting to event
   * metadata (e.g. the bus's namespace).
   *
   * @param {String} name The name of event to emit
   * @param {String[]} args 0 or more additional data arguments to deliver with the event
   * @returns {Events} This Events instance
   */
  proto.emit = function (name) {
    return this._emitEvent(this._event.apply(this, arguments));
  };

  /**
   * Creates an opaque event object from an argument list containing at least a name, and optionally additional
   * event payload arguments.
   *
   * @param {String} name The name of event to emit
   * @param {String[]} args 0 or more additional data arguments to deliver with the event
   * @returns {Object} A new event object
   * @private
   */
  proto._event = function (name) {
    return {
      name: name,
      args: [].slice.call(arguments, 1),
      attrs: {},
      source: {
        key: this._key,
        origin: this._origin
      }
    };
  };

  /**
   * Emits a previously-constructed event object to all listeners.
   *
   * @param {Object} event The event object to emit
   * @param {String} event.name The name of the event
   * @param {Object} event.source Metadata about the original source of the event, containing key and origin
   * @param {Array} event.args The args passed to emit, to be delivered to listeners
   * @returns {Events} This Events instance
   * @private
   */
  proto._emitEvent = function (event) {
    var args = event.args.concat(event);
    fire(this._listeners(event.name), args);
    fire(this._any, [event.name].concat(args));
    return this;
  };

  /**
   * Returns an array of listeners by event name, creating a new name array if none are found.
   *
   * @param {String} name The event name for which listeners should be returned
   * @returns {Array} An array of listeners; empty if none are registered
   * @private
   */
  proto._listeners = function (name) {
    return this._events[name] = this._events[name] || [];
  };

  // Internal helper for firing an event to an array of listeners
  function fire(listeners, args) {
    $.each(listeners, function () {
      try {
        this.apply(null, args);
      } catch (e) {
        log(e.stack || e.message || e);
      }
    });
  }

  return {
    Events: Events
  };

});

/*

// mocha tests -- port to qunit when ready

var assert = require('assert');
var Events = require('./_events');

describe('Events', function () {

  var bus;

  beforeEach(function () {
    bus = new Events();
  });

  it('should emit basic events', function (done) {
    bus.on('foo', function () {
      assert(true);
      done();
    });
    bus.emit('foo');
  });

  it('should emit events with arbitrary arguments', function (done) {
    bus.on('foo', function (a, b, c) {
      assert.equal(a, 1);
      assert.equal(b, 2);
      assert.equal(c, 3);
      done();
    });
    bus.emit('foo', 1, 2, 3);
  });

  it('should emit events with an event object as the last argument', function (done) {
    bus.on('foo', function (a, b, c, event) {
      assert.equal(a, 1);
      assert.equal(b, 2);
      assert.equal(c, 3);
      assert.equal(typeof event, 'object');
      assert.equal(event.name, 'foo');
      assert.equal(event.source.origin, 'local');
      assert.deepEqual(event.args, [1, 2, 3]);
      bus.on('bar', function (event2) {
        assert.equal(typeof event2, 'object');
        assert.equal(event2.name, 'bar');
        assert.equal(event2.source.origin, 'local');
        assert.deepEqual(event2.args, []);
        done();
      });
      bus.emit('bar');
    });
    bus.emit('foo', 1, 2, 3);
  });

  it('should list active event names', function () {
    function a() { console.log('a'); }
    bus.on('foo', a);
    function b() { console.log('b'); }
    bus.on('bar', b);
    function c() { console.log('c'); }
    bus.on('baz', c);
    assert.deepEqual(bus.active(), ['foo', 'bar', 'baz']);
    bus.off('bar', b);
    assert.deepEqual(bus.active(), ['foo', 'baz']);
    bus.offAll();
    assert.deepEqual(bus.active(), []);
  });

  it('should list all listeners for an event name', function () {
    function a() { console.log('a'); }
    bus.on('foo', a);
    function b() { console.log('b'); }
    bus.on('foo', b);
    function c() { console.log('c'); }
    bus.on('foo', c);
    assert.deepEqual(bus.listeners('foo'), [a, b, c]);
  });

  it('should only execute a "once" listener once', function (done) {
    function once() { done(); }
    bus.once('foo', once);
    bus.emit('foo');
    bus.emit('foo');
  });

  it('should fire an any listener on any event, with all expected arguments', function (done) {
    var count = 0;
    bus.onAny(function (name, a, b, c, event) {
      assert.ok(name === 'foo' || name === 'bar'); // meh
      assert.equal(a, 1);
      assert.equal(b, 2);
      assert.equal(c, 3);
      assert.equal(typeof event, 'object');
      assert.equal(event.source.origin, 'local');
      assert.deepEqual(event.args, [1, 2, 3]);
      count++;
    });
    bus.emit('foo', 1, 2, 3);
    bus.emit('bar', 1, 2, 3);
    assert.equal(count, 2);
    done();
  });

  it('should allow listeners to be removed', function () {
    var count = 0;
    function listener() { count++; }
    bus.on('foo', listener);
    bus.emit('foo');
    bus.off('foo', listener);
    bus.emit('foo');
    assert.equal(count, 1);
  });

  it('should allow all listeners to be removed by name', function () {
    var count1 = 0, count2 = 0;
    function listener1() { count1++; }
    function listener2() { count2++; }
    bus.on('foo', listener1).on('foo', listener2);
    bus.emit('foo');
    bus.offAll('foo');
    bus.emit('foo');
    assert.equal(count1, 1);
    assert.equal(count2, 1);
  });

  it('should allow all listeners to be removed', function () {
    var count1 = 0, count2 = 0;
    function listener1() { count1++; }
    function listener2() { count2++; }
    bus.on('foo', listener1).on('foo', listener2);
    bus.on('bar', listener1).on('bar', listener2);
    bus.emit('foo').emit('bar');
    bus.offAll();
    bus.emit('foo').emit('bar');
    assert.equal(count1, 2);
    assert.equal(count2, 2);
  });

  it('should allow an any listener to be removed', function () {
    var count = 0;
    function listener() { count++; }
    bus.onAny(listener);
    bus.emit('foo');
    bus.offAny(listener);
    bus.emit('foo');
    assert.equal(count, 1);
  });

  // it('should ', function (done) {
  // });

});

*/
