context = appContext.getBean("renderContext").toContextMap()

{mash} = require "vendor/underscore"

exports = module.exports = mash ([k, context.get(k)] for k in context.keySet().toArray())
