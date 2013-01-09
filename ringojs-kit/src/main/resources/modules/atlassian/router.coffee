Request = require "atlassian/request"
Response = require "atlassian/response"
context = require "atlassian/context"
{merge, mash} = require "vendor/underscore"

methods = ["get", "post", "put", "del"]

module.exports =

  createApp: (appDir) ->

    routes = {}
    routes[method] = [] for method in methods
    options = {}

    app = (jsgiReq) ->
      req = new Request jsgiReq, options
      res = new Response appDir, options
      handler = null
      for route in routes[req.method.toLowerCase()]
        match = route.pattern.exec req.pathInfo
        if match
          handler = route.handler
          req.params = mash route.params, (k, i) -> [k, match[i + 1]]
          break
      handler ?= -> res.sendNotFound jsgiReq.scriptName
      handler req, res
      res.toJSON()

    app.configure = (opts) ->
      options = merge options, opts
      app

    app.all = (route, handler) ->
      app[method](route, handler) for method in methods
      app

    for method in methods
      do (method) ->
        app[method] = (pattern, handler) ->
          params = []
          re = pattern
            .replace(/[-\[\]{}()*+?.,\\^$|#\s]/g, "\\$&")
            .replace(/\/\\\*$/g, "(/.*|$)")
            .replace(/\/:([\w\d\-_]+)/g, (_, $1) ->
              params.push $1
              "/([^/]+)"
            )
          routes[method].push {pattern: new RegExp("^#{re}$"), params, handler}
          app

    app
