importClass com.atlassian.util.concurrent.Promises
importClass com.atlassian.util.concurrent.Promise
importClass com.google.common.util.concurrent.SettableFuture
{proxy} = require "./util"
{extend} = require "vendor/underscore"

# Promises / Deferred Mixin
#
#     var Deferred = require("atlassian/promises").Deferred;
#
# @mixin
#
Deferred =
  # Creates a promise. Provides a way to execute callback functions based on one or more objects,
  # usually Deferred objects that represent asynchronous events.
  # @param [Deferreds] first One or more Deferred objects, or plain JavaScript objects.
  # @return [Promise] promise
  when: (first) ->
    args = arg for arg in (if first instanceof Array then first else arguments)
    Promises.when args...

  # Alias of 'when' for use coffeescript where 'when' is a keyword
  all: -> exports.when arguments...

  # A constructor function that returns a chainable utility object with methods to register
  # multiple callbacks into callback queues, invoke callback queues, and relay the success
  # or failure state of any synchronous or asynchronous function.
  Deferred: ->
    future = SettableFuture.create()
    promise = Promises.forFuture future
    proxy future,
      resolve: -> future.set arguments...; @
      reject: -> future.setException arguments...; @
      promise: -> promise

exports = module.exports = Deferred