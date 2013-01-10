context = appContext.getBean "renderContext"

{mash} = require "vendor/underscore"
{proxy} = require "atlassian/util"

module.exports = do ->
  proxy context,
    toJSON: ->
      mash ([e.key, e.value] for e in context.toContextMap().entrySet().toArray())
