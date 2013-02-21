Request = require "atlassian/request"
Response = require "atlassian/response"
{merge, mash} = require "vendor/underscore"

# Main Ringo Application Router
#
# main.js/main.coffee must create an instance of the app like so:
#
#     var app = require("atlassian/app").create();
#
# or:
#
#     var App = require("atlassian/app");
#     var app = new App();
#
# @method #configure(opts)
#   Configures the app instance
#   @param [Object] opts A hash of options
#   @option opts [Array<String>] stylesheets Stylesheets to be added to the page
#   @option opts [Array<String>] scripts JavaScript sources to be added to the page
#   @return [Object] App instance
#
# @method #get(pattern, handler)
#   GET request
#   @param [String] pattern Path or pattern for route (e.g., /users)
#   @param [Function] handler Callback function to execute when route matches
#
# @method #post(pattern, handler)
#   POST request
#   @param [String] pattern Path or pattern for route (e.g., /users)
#   @param [Function] handler Callback function to execute when route matches
#
# @method #put(pattern, handler)
#   PUT request
#   @param [String] pattern Path or pattern for route (e.g., /users)
#   @param [Function] handler Callback function to execute when route matches
#
# @method #delete(pattern, handler)
#   DELETE request
#   @param [String] pattern Path or pattern for route (e.g., /users)
#   @param [Function] handler Callback function to execute when route matches
#
# @method #all(pattern, handler)
#   ALL request (i.e., handle GET, POST, PUT, and DELETE)
#   @param [String] pattern Path or pattern for route (e.g., /users)
#   @param [Function] handler Callback function to execute when route matches
module.exports = class App

  # Instantiates the Ringo app
  # @param [String] appDir Root directory where the app resides
  # @return [Object] App instance
  constructor: (appDir) ->
    methods = ["get", "post", "put", "del"]
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
      try
        handler req, res
      catch ex
        res.renderError ex
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

    return app

  # Creates an app. Provided as a convenience function to allow a one-liner
  # instantiation:
  #
  #     var app = require('atlassian/app').create()
  #
  # @param [String] appDir Root directory of the app
  # @return [Object] App instance
  @create: (appDir) ->
    new App(appDir)