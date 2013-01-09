{Deferred} = require "atlassian/promises"
{publicUrl, resourcePath, renderTemplate} = require "atlassian/util"
context = require "atlassian/context"
{merge} = require "vendor/underscore"

module.exports = (appDir, options) ->

  deferred = new Deferred

  data =
    status: 200
    headers:
      "Content-Type": "text/html"
    body: []

  appDir = appDir or "./"
  appDir += "/" if appDir.charAt(appDir.length - 1) isnt "/"

  # response.writeHead(statusCode, headers)
  # response.writeHead(headers)
  writeHead: (statusCode, headers) ->
    if typeof statusCode is "object"
      headers = statusCode
    else
      data.status = statusCode
    if headers
      data.headers[k] = v for own k, v of headers

  # response.write(chunk)
  write: (chunk) ->
    data.body.push chunk

  # response.end()
  # response.end(chunk)
  end: (chunk) ->
    @write chunk if chunk?
    deferred.resolve data

  # response.send(body)
  # response.send(body, headers)
  # response.send(body, headers, statusCode)
  send: (body, headers, statusCode) ->
    @sendWithLayout "layout", body, headers, statusCode

  # response.send(path)
  # response.send(path, headers)
  sendNotFound: (path, headers) ->
    @renderError ("Not Found#{if path then ': ' + path else ''}"), headers, 404

  # response.send(layout, body)
  # response.send(layout, body, headers)
  # response.send(layout, body, headers, statusCode)
  sendWithLayout: (layout, body, headers={}, statusCode=200) ->
    try
      resType = if options.aui then 'aui' else 'default'
      # @todo validate that options.aui contains a valid version number
      resLocals = merge context,
        aui: (if options.aui then "v#{options.aui.replace('.', '_')}" else null)
      locals = merge resLocals,
        stylesheetUrls: ("#{publicUrl 'css', path}" for path in options.stylesheets)
        scriptUrls: ("#{publicUrl 'js', path}" for path in options.scripts)
        head: renderView(appDir, "layout-head-#{resType}", resLocals)
        tail: renderView(appDir, "layout-tail-#{resType}", resLocals)
        body: body
      page = if layout then renderView appDir, layout, locals else body
      @writeHead statusCode, headers
      @end page
    catch ex
      if layout is "layout"
        err = (if ex.stack then "#{ex.message}\n#{ex.stack}" else ex.toString())
        throw new Error "Unable to render default layout: #{err}"
      else
        @renderError ex, headers, statusCode

  # response.sendJson(data)
  # response.sendJson(data, headers)
  # response.sendJson(data, headers, statusCode)
  sendJson: (data, headers={}, statusCode=200) ->
    @writeHead statusCode, merge({"Content-Type": "application/json; charset=UTF-8"}, headers)
    @end (if typeof data is "string" then data else JSON.stringify(data or {}))

  # response.render(view, locals)
  # response.render(view, locals, headers, statusCode)
  render: (view, locals, headers, statusCode) ->
    @renderWithLayout "layout", view, locals, headers, statusCode

  # response.renderWithLayout(layout, view, locals)
  # response.renderWithLayout(layout, view, locals, headers, statusCode)
  renderWithLayout: (layout, view, locals, headers, statusCode) ->
    try
      body = renderView appDir, view, merge(context, locals)
    catch ex
      @renderErrorWithLayout layout, ex, headers, statusCode
    @sendWithLayout layout, body, headers, statusCode

  # response.renderError(error)
  # response.renderError(error, headers)
  # response.renderError(error, headers, statusCode)
  renderError: (error, headers, statusCode) ->
    @renderErrorWithLayout "layout", error, headers, statusCode

  # response.renderErrorWithLayout(layout, error)
  # response.renderErrorWithLayout(layout, error, headers)
  # response.renderErrorWithLayout(layout, error, headers, statusCode)
  renderErrorWithLayout: (layout, error, headers, statusCode) ->
    je = error.javaException
    locals =
      error:
        if je and je.cause
          je.cause.getMessage()
        else if error.getMessage
          error.getMessage()
        else if error.stack
          "#{error.message}\n#{error.stack}"
        else
          error?.toString() or "Unknown cause"
    @renderWithLayout layout, "error", locals, headers, statusCode

  toJSON: ->
    deferred.promise().claim()

renderView = (appDir, view, locals) ->
  html = renderTemplate "#{appDir}views/#{view}", locals
  html = renderTemplate "views/#{view}", locals if not html?
  throw new Error "No template found for view path '#{view}'" if not html?
  html
