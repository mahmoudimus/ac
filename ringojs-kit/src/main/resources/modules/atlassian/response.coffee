{Deferred} = require "atlassian/promises"
{publicUrl, resourcePath, renderTemplate} = require "atlassian/util"
context = require "atlassian/context"
{merge} = require "vendor/underscore"
bigpipe = require "atlassian/http/bigpipe"

module.exports = (appDir, options) ->

  deferred = new Deferred

  data =
    status: 200
    headers:
      "content-type": "text/html"
    body: []

  appDir = appDir or "./"
  appDir += "/" if appDir.charAt(appDir.length - 1) isnt "/"

  # @todo from expressjs: format, cookie stuff, maybe jsonp

  # sets a header
  # response.set(name, value)
  set: (name, value) ->
    data.headers[name.toLowerCase()] = value

  # gets a header
  # response.get(name)
  get: (name) ->
    return v for k, v of data.headers when k is name?.toLowerCase()

  # sets the Content-Type header, with alias support (html, json, xml)
  # response.type(type)
  type: (type) ->
    @set "content-type", switch type
      when "html" then "text/html"
      when "json" then "application/json"
      when "xml" then "application/xml"
      else type

  # sends a redirect to the specified location with optional statusCode
  # response.sendRedirect(location)
  # response.sendRedirect(location, statusCode)
  sendRedirect: (location, statusCode=302) ->
    # @todo accept relative locations
    @writeHead 302, Location: location
    @end()

  # writes the head of the response with statusCode and header
  # response.writeHead(statusCode, headers)
  # response.writeHead(headers)
  writeHead: (statusCode, headers) ->
    if typeof statusCode is "object"
      headers = statusCode
    else
      data.status = statusCode
    if headers
      data.headers[k.toLowerCase()] = v for k, v of headers

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
    @writeHead 404, headers
    @end "Not Found#{if path then ': ' + path else ''}"

  # response.send(layout, body)
  # response.send(layout, body, headers)
  # response.send(layout, body, headers, statusCode)
  sendWithLayout: (layout, body, headers={}, statusCode=200) ->
    try
      @writeHead statusCode, headers
      page =
        if layout
          resType = if options.aui then 'aui' else 'default'
          # @todo validate that options.aui contains a valid version number
          auiLocals = merge context.toJSON(),
            aui: (if options.aui then "v#{options.aui.replace('.', '_')}" else null)
          layoutLocals = merge auiLocals,
            stylesheetUrls: ("#{publicUrl 'css', path}" for path in options.stylesheets or [])
            scriptUrls: ("#{publicUrl 'js', path}" for path in options.scripts or [])
            clientOptions: ("#{k}:#{v}" for k, v of options.clientOptions).join(";")
            head: renderView(appDir, "layout-head-#{resType}", auiLocals)
            tail: renderView(appDir, "layout-tail-#{resType}", auiLocals)
            bigPipeReadyContents: bigpipe.consumeContent()
            body: body
          renderView appDir, layout, layoutLocals
        else
          body
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
    @type "json"
    @writeHead statusCode, headers
    @end (if typeof data is "string" then data else JSON.stringify(data or {}))

  # response.render(view, locals)
  # response.render(view, locals, headers, statusCode)
  render: (view, locals, headers, statusCode) ->
    @renderWithLayout "layout", view, locals, headers, statusCode

  # response.renderWithLayout(layout, view, locals)
  # response.renderWithLayout(layout, view, locals, headers, statusCode)
  renderWithLayout: (layout, view, locals, headers, statusCode) ->
    try
      locals = merge context.toJSON(), locals
      body = renderView appDir, view, locals
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
    error =
      if je and je.cause
        je.cause.getMessage()
      else if error.getMessage
        error.getMessage()
      else if error.stack
        "#{error.message}\n#{error.stack}"
      else
        error?.toString() or "Unknown cause"
    @renderWithLayout layout, "error", {error: error?.trim()}, headers, statusCode

  toJSON: ->
    deferred.promise().claim()

renderView = (appDir, view, locals) ->
  html = renderTemplate "#{appDir}views/#{view}", locals
  html = renderTemplate "views/#{view}", locals if not html?
  throw new Error "No template found for view path '#{view}'" if not html?
  html
