{mash, merge} = require "vendor/underscore"
context = require "atlassian/context"

module.exports = (req, options) ->

  pathSplit = req.scriptName.indexOf "/", 1

  decode = (n, v) -> [decodeURIComponent(n), if v? then decodeURIComponent(v) else null]
  query = mash (decode nv.split("=")... for nv in (req.queryString or "").split("&"))

  merge req,
    pathInfo: req.scriptName.slice(pathSplit)
    scriptName: req.scriptName.slice(0, pathSplit)
    query: query
    context: context
