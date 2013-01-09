importClass com.atlassian.util.concurrent.Promises
importClass com.google.common.util.concurrent.SettableFuture
{delegate} = require "./util"

exports = module.exports =

  # @todo DEBUG when/all

  # creates a new promise from
  when: ->
    args = for arg in arguments
      if arg._delegate instanceof Promise then arg.delegate
      else if arg.promise then arg.promise()
      else if arg.toPromise then arg.toPromise()
      else arg
    Promises.when args...

  all: => exports.when arguments...

  # creates a new deferred object convertable to a promise
  Deferred: ->
    future = SettableFuture.create()

    deferred = delegate future
    deferred.resolve = -> future.set arguments...
    deferred.reject = -> future.setException arguments...

    promise = delegate Promises.forFuture(future)
    promise.pipe = -> promise.map arguments...
    deferred.promise = -> promise

    deferred
