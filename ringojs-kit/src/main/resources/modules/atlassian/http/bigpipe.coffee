{proxy} = require "atlassian/util"

# @todo refactor BigPipe Java API to work something closer to this
bp = module.exports = proxy appContext.getBean("bigPipe")
