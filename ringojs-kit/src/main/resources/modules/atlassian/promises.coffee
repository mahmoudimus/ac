importClass com.atlassian.util.concurrent.Promises
importClass com.google.common.util.concurrent.SettableFuture
{proxy} = require "./util"

exports = module.exports =

  # creates a new promise from
  when: ->
    args = for arg in arguments
      if arg._delegate instanceof Promise then arg._delegate
      else if typeof arg.promise is "function" then arg.promise()
      else if typeof arg.toPromise is "function" then arg.toPromise()
      else arg
    Promises.when args...

  all: => exports.when arguments...

  # creates a new deferred object convertable to a promise
  Deferred: ->
    future = SettableFuture.create()

    deferred = proxy future
    deferred.resolve = -> future.set arguments...
    deferred.reject = -> future.setException arguments...

    promise = proxy Promises.forFuture(future)
    promise.pipe = -> promise.map arguments...
    deferred.promise = -> promise

    deferred
