importClass com.atlassian.util.concurrent.Promises
importClass com.atlassian.util.concurrent.Promise
importClass com.google.common.util.concurrent.SettableFuture
{proxy} = require "./util"

exports = module.exports =

  # creates a new promise representing one or more other promises
  when: (first) ->
    args = arg for arg in (if first instanceof Array then first else arguments)
    Promises.when args...

  # alias of 'when' for use coffeescript where 'when' is a keyword
  all: -> exports.when arguments...

  # creates a new deferred object convertable to a promise
  Deferred: ->
    future = SettableFuture.create()
    promise = Promises.forFuture future
    proxy future,
      resolve: -> future.set arguments...; @
      reject: -> future.setException arguments...; @
      promise: -> promise
