(function (global, exports) {

  function each(a, it) {
    for (var i = 0, l = a.length; i < l; i += 1) {
      if (it(a[i], i) === false) {
        return;
      }
    }
  }

  function slice(a, start, end) {
    return [].slice.call(a, start || 0, end || a.length);
  }

  function toArray(a) {
    return a instanceof Array ? a : (a == null ? [] : (a.prototype ? [a] : (a.callee ? slice(a) : [a])));
  }

  exports.all = exports.when = function (first) {
    var args = slice(arguments),
        l = args.length,
        count = l,
        deferred = exports.Deferred();
    if (l === 0 || (l === 1 && (!first || typeof first.promise !== "function"))) {
      deferred.resolveWith(deferred, args);
    }
    else {
      each(args, function (arg, i) {
        if (arg && typeof arg.promise === "function") {
          arg.promise().then(function () {
            var a = slice(arguments);
            args[i] = a.length > 1 ? a : a[0];
            count -= 1;
            if (!count) {
              deferred.resolveWith(deferred, args);
            }
          }, deferred.reject);
        }
        else {
          count -= 1;
        }
      });
    }
    return deferred.promise();
  };

  exports.Deferred = function () {

    var onAlways = [],
        onDone = [],
        onFail = [],
        settledState,
        settledArgs,
        settledContext;

    var enqueue = function (queue, callbacks) {
      queue.push.apply(queue, toArray(callbacks));
    };

    var fire = function (callbacks) {
      each(toArray(callbacks), function (callback) {
        callback.apply(settledContext, settledArgs);
      });
    };

    var join = function (queue, callbacks, fireIfSettled) {
      callbacks = toArray(callbacks);
      if (settledState == null) {
        enqueue(queue, callbacks);
      }
      else if (fireIfSettled) {
        fire(callbacks);
      }
    };

    var settle = function (state, context, args, queue) {
      if (settledState == null) {
        settledState = state;
        settledContext = context || global;
        settledArgs = toArray(args);
        if (queue) fire(queue);
        if (onAlways) fire(onAlways);
        onAlways = onDone = onFail = null;
      }
    };

    var promise = {

      always: function (callbacks) {
        join(onAlways, callbacks, promise.isResolved() || promise.isRejected());
        return this;
      },

      done: function (callbacks) {
        join(onDone, callbacks, promise.isResolved());
        return this;
      },

      fail: function (callbacks) {
        join(onFail, callbacks, promise.isRejected());
        return this;
      },

      isRejected: function () {
        return settledState === false;
      },

      isResolved: function () {
        return settledState === true;
      },

      promise: function () {
        return promise;
      },

      then: function (doneCallbacks, failCallbacks) {
        promise.done(doneCallbacks).fail(failCallbacks);
        return this;
      }

    };

    var deferred = {

      reject: function () {
        return deferred.rejectWith(deferred, arguments);
      },

      rejectWith: function (context, args) {
        settle(false, context, args, onFail);
        return deferred;
      },

      resolve: function () {
        return deferred.resolveWith(deferred, arguments);
      },

      resolveWith: function (context, args) {
        settle(true, context, args, onDone);
        return deferred;
      }

    };

    // adds a wait fn for execution in ringojs
    if (java && sync) {
      var lock = new java.lang.Object(),
          complete = settle;
      settle = sync(function () {
        complete.apply(null, arguments);
        lock.notifyAll();
      }, lock);
      promise.wait = sync(function (timeout) {
        if (settledState == null) {
          lock.wait(timeout || 0);
        }
      }, lock);
    }

    for (var k in promise) {
      if (promise.hasOwnProperty(k)) {
        deferred[k] = promise[k];
      }
    }

    return deferred;

  };

}(this, this.RA || exports));
