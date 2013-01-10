context = appContext.getBean("renderContext")

{merge, mash} = require "vendor/underscore"
{proxy} = require "atlassian/util"

module.exports = do ->
  merge (proxy context),
    toJSON: ->
      mash ([e.key, e.value] for e in context.toContextMap().entrySet().toArray())
