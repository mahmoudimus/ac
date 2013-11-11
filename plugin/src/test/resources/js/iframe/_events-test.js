define(['iframe/_events'], function() {

  (window.AP || window._AP).require(["_events", "_dollar"], function(events, $) {

    var Events = events.Events;

    module('Events');

    test('emits basic events', function () {
      var bus = new Events();
      var spy = sinon.spy();

      bus.on('foo', spy);
      bus.emit('foo');

      ok(spy.calledOnce, "Spy was called");
    });

    test('emits events with arbitrary arguments', function() {
      var bus = new Events();
      var spy = sinon.spy();

      bus.on('foo', spy);
      bus.emit('foo', 1, 2, 3);

      ok(spy.withArgs(1, 2, 3), "Args were passed to spy");
    });

    test('emits events with an event object as the last argument', function() {
      var bus = new Events();
      var spy = sinon.spy();

      bus.on('foo', spy);
      bus.emit('foo', 1, 2, 3);

      var event = spy.firstCall.args[3];
      equal(typeof event, 'object');
      equal(event.name, 'foo');
      deepEqual(event.args, [1, 2, 3]);
    });

    test('emits events with the key and origin that were passed to the constructor', function() {
      var bus = new Events('my-key', 'my-origin');
      var spy = sinon.spy();

      bus.on('foo', spy);
      bus.emit('foo');

      var event = spy.firstCall.args[0];
      equal(event.source.origin, 'my-origin');
      equal(event.source.key, 'my-key');
    });

    test('emits events for multiple handlers', function() {
      var bus = new Events();
      var spy1 = sinon.spy();
      var spy2 = sinon.spy();

      bus.on('foo', spy1);
      bus.on('foo', spy2);
      bus.emit('foo');

      ok(spy1.calledOnce);
      ok(spy2.calledOnce);
    });

    test('does not collide events with different names', function() {
      var bus = new Events();
      var spy1 = sinon.spy();
      var spy2 = sinon.spy();

      bus.on('foo', spy1);
      bus.on('bar', spy2);
      bus.emit('bar');
      bus.emit('foo');

      ok(spy1.calledOnce);
      ok(spy2.calledOnce);
    });

    test('only executes a "once" listener once', function() {
      var bus = new Events();
      var spy = sinon.spy();

      bus.once('foo', spy);
      bus.emit('foo');
      bus.emit('foo');

      ok(spy.calledOnce);
    });

    test('fires an "onAny" listener on any event', function() {
      var bus = new Events();
      var spy = sinon.spy();

      bus.onAny(spy);
      bus.emit('foo');
      bus.emit('bar');

      equal(spy.callCount, 2);
    });

    test('fires an "onAny" listener with all expected arguments', function() {
      var bus = new Events('my-key', 'my-origin');
      var spy = sinon.spy();

      bus.onAny(spy);
      bus.emit('foo', 1, 2, 3);

      ok(spy.withArgs(1, 2, 3), "Args were passed to spy");
      var event = spy.firstCall.args[4];
      equal(typeof event, 'object');
      equal(event.name, 'foo');
      deepEqual(event.args, [1, 2, 3]);
      equal(event.source.origin, 'my-origin');
      equal(event.source.key, 'my-key');
    });

    test('allows listeners to be removed', function() {
      var bus = new Events();
      var spy = sinon.spy();

      bus.on('foo', spy);
      bus.emit('foo');
      bus.off('foo', spy);
      bus.emit('foo');

      equal(spy.callCount, 1);
    });

    test('allows all listeners to be removed by name', function () {
      var bus = new Events();
      var spy1 = sinon.spy();
      var spy2 = sinon.spy();

      bus.on('foo', spy1).on('foo', spy2);
      bus.emit('foo');
      bus.offAll('foo');
      bus.emit('foo');

      equal(spy1.callCount, 1);
      equal(spy2.callCount, 1);
    });

    test('only removes listeners by name', function () {
      var bus = new Events();
      var spy = sinon.spy();

      bus.on('foo', spy);
      bus.offAll('bar');
      bus.emit('foo');

      equal(spy.callCount, 1);
    });

    test('allows all listeners to be removed', function () {
      var bus = new Events();
      var spy1 = sinon.spy();
      var spy2 = sinon.spy();

      bus.on('foo', spy1).on('foo', spy2);
      bus.on('bar', spy1).on('bar', spy2);
      bus.emit('foo').emit('bar');
      bus.offAll();
      bus.emit('foo').emit('bar');

      equal(spy1.callCount, 2);
      equal(spy2.callCount, 2);
    });

    test('allows an any listener to be removed', function () {
      var bus = new Events();
      var spy = sinon.spy();

      bus.onAny(spy);
      bus.emit('foo');
      bus.offAny(spy);
      bus.emit('foo');

      ok(spy.calledOnce);
    });
  });
});

