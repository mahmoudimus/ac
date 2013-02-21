{Deferred} = require "atlassian/promises"
{devMode, resourcePath, renderTemplate} = require "atlassian/util"
context = require "atlassian/context"
{merge} = require "vendor/underscore"
bigpipe = require "atlassian/http/bigpipe"

# Response object
module.exports = class Response
  parseResourcePaths = (type, paths) ->
    if typeof paths is "string"
      paths = paths.split(",").filter((s) -> !!s).map((s) -> s.trim())
    ("#{publicUrl type, path}" for path in paths or [])

  publicUrl = (type, path) ->
    exts = [".min.#{type}", "-min.#{type}"]
    exts[if devMode then "unshift" else "push"] ".#{type}"
    for ext in exts
      file = resourcePath "public/#{type}/#{path}", ext
      return file if file
    throw new Error "No #{type} resource found for path '#{path}'"

  renderView = (appDir, view, locals) ->
    html = renderTemplate "#{appDir}views/#{view}", locals
    html = renderTemplate "views/#{view}", locals if not html?
    throw new Error "No template found for view path '#{view}'" if not html?
    html

  # @param [String] appDir the application's root directory
  # @param [Object] options options object
  # @option options [String] aui Version of AUI to include in page
  # @option options [Array<String>] stylesheets Additional stylesheets to add to the page
  # @option options [Array<String>] scripts Additional scripts to add to the page
  # @option options [Object] clientOptions Additional options for rendering the client
  constructor: (@appDir, @options) ->
    @deferred = new Deferred

    @data =
      status: 200
      headers:
        "content-type": "text/html"
      body: []

    @appDir = @appDir or "./"
    @appDir += "/" if @appDir.charAt(@appDir.length - 1) isnt "/"

  # @todo from expressjs: format, cookie stuff, maybe jsonp

  # Sets a header
  # @param [String] name Name of the header to set
  # @param [String] value Value of the header to set
  set: (name, value) ->
    @data.headers[name.toLowerCase()] = value

  # Gets a header
  # @param [String] name Name of the header to get
  # @return [String] Value of header
  get: (name) ->
    return v for k, v of @data.headers when k is name?.toLowerCase()

  # Sets the Content-Type header, with alias support (text, html, json, xml)
  # @param [String] type Content-type to set the response to
  type: (type) ->
    @set "content-type", switch type
      when "text" then "text/plain"
      when "html" then "text/html"
      when "json" then "application/json"
      when "xml" then "application/xml"
      else type

  # Sends a redirect to the specified location with optional statusCode
  #
  # @overload sendRedirect(location)
  #   Sends redirect header using supplied URL with a 302 status code
  #   @param [String] location Relative URL to redirect to
  # @overload sendRedirect(location, statusCode=302)
  #   Sends redirect header using supplied URL and statusCode
  #   @param [String] location Relative URL to redirect to
  #   @param [Integer] statusCode Status code to return
  sendRedirect: (location, statusCode=302) ->
    # @todo accept relative locations
    @writeHead 302, Location: location
    @end()

  # Writes the head of the response with statusCode and header
  # response.writeHead(statusCode, headers)
  # response.writeHead(headers)
  #
  # @overload writeHead(headers)
  #   @param [Object] headers Headers to write in the response
  # @overload writeHead(statusCode, headers)
  #   @param [Integer] statusCode Response status code to write in the response
  #   @param [Object] headers Headers to write in the response
  writeHead: (statusCode, headers) ->
    if typeof statusCode is "object"
      headers = statusCode
    else
      @data.status = statusCode
    if headers
      @data.headers[k.toLowerCase()] = v for k, v of headers

  # Pushes a chunk of data into the body. Must be followed by `.end()`
  # @param [String] chunk String to write into body
  write: (chunk) ->
    @data.body.push chunk

  # Ends the response. Used after `.write()`
  # @param [String] chunk Optional string to write out before resolving the response
  end: (chunk) ->
    @write chunk if chunk?
    @deferred.resolve @data

  # Sends response
  #
  # @overload send(body)
  #   @param [String] body String to send in the body
  # @overload send(body, headers)
  #   @param [String] body String to send in the body
  #   @param [Object] headers Headers to send in the response
  # @overload send(body, headers, statusCode)
  #   @param [String] body String to send in the body
  #   @param [Object] headers Headers to send in the response
  #   @param [Integer] statusCode Response status code to send in the response
  send: (body, headers, statusCode) ->
    @sendWithLayout "layout", body, headers, statusCode

  # Sends response with layout
  #
  # @overload sendWithLayout(layout, body)
  #   @param [String] layout Name of layout to render
  #   @param [String] body String to render inside the layout
  # @overload sendWithLayout(layout, body, headers)
  #   @param [String] layout Name of layout to render
  #   @param [String] body String to render inside the layout
  #   @param [Object] headers Headers to send in the response
  # @overload sendWithLayout(layout, body, headers, statusCode)
  #   @param [String] layout Name of layout to render
  #   @param [String] body String to render inside the layout
  #   @param [Object] headers Headers to send in the response
  #   @param [Integer] statusCode Response status code to send in the response
  sendWithLayout: (layout, body, headers={}, statusCode=200) ->
    try
      @writeHead statusCode, headers
      page =
        if layout
          resType = if @options.aui then 'aui' else 'default'
          # @todo validate that @options.aui contains a valid version number
          auiLocals = merge context.toJSON(),
            aui: (if @options.aui then "v#{@options.aui.replace('.', '_')}" else null)
          layoutLocals = merge auiLocals,
            stylesheetUrls: parseResourcePaths("css", @options.stylesheets)
            scriptUrls: parseResourcePaths("js", @options.scripts)
            clientOptions: ("#{k}:#{v}" for k, v of @options.clientOptions).join(";")
            head: renderView(@appDir, "layout-head-#{resType}", auiLocals)
            tail: renderView(@appDir, "layout-tail-#{resType}", auiLocals)
            bigPipeReadyContents: bigpipe.consumeContent()
            body: body
          renderView @appDir, layout, layoutLocals
        else
          body
      @end page
    catch ex
      if layout is "layout"
        err = (if ex.stack then "#{ex.message}\n#{ex.stack}" else ex.toString())
        throw new Error "Unable to render default layout: #{err}"
      else
        @renderError ex, headers, statusCode

  # Sends a response with content-type: text/plain
  #
  # @overload sendText(text)
  #   @param [String] text String to send in the response
  # @overload sendText(text, headers)
  #   @param [String] text String to send in the response
  #   @param [Object] headers Headers to send in the response
  # @overload sendText(text, headers, statusCode)
  #   @param [String] text String to send in the response
  #   @param [Object] headers Headers to send in the response
  #   @param [Integer] statusCode Response status code to send in the response
  sendText: (text, headers={}, statusCode=200) ->
    @type "text"
    @writeHead statusCode, headers
    @end text

  # Sends a 404 not found
  #
  # @overload sendNotFound(path)
  #   @param [String] path Path of the object that's not found
  # @overload sendNotFound(path, headers)
  #   @param [String] path Path of the object that's not found
  #   @param [Object] headers Headers to send in the response
  sendNotFound: (path, headers) ->
    @sendText "Not Found#{if path then ': ' + path else ''}", headers, 404

  # Sends JSON
  #
  # @overload sendJson(data)
  #   @param [String|Object] data JSON object to send
  # @overload sendJson(data, headers)
  #   @param [String|Object] data JSON object to send
  #   @param [Object] headers Headers to send in the response
  # @overload sendJson(data, headers, statusCode=200)
  #   @param [String|Object] data JSON object to send
  #   @param [Object] headers Headers to send in the response
  #   @param [Integer] statusCode Response status code
  sendJson: (data, headers={}, statusCode=200) ->
    @type "json"
    @writeHead statusCode, headers
    @end (if typeof data is "string" then data else JSON.stringify(data or {}))

  # Render a view
  #
  # @overload render(view, locals)
  #   @param [String] view Name of view to render
  #   @param [Object] locals Context object to pass to the view
  # @overload render(view, locals, headers, statusCode)
  #   @param [String] view Name of view to render
  #   @param [Object] locals Context object to pass to the view
  #   @param [Object] headers Headers to send in the response
  #   @param [Integer] statusCode Response status code
  render: (view, locals, headers, statusCode) ->
    @renderWithLayout "layout", view, locals, headers, statusCode

  # Render with layout
  #
  # @overload renderWithLayout(layout, view, locals)
  #   @param [String] layout Layout to use
  #   @param [String] view Name of view to render
  #   @param [Object] locals Context object to pass to the view
  # @overload renderWithLayout(layout, view, locals, headers, statusCode)
  #   @param [String] layout Layout to use
  #   @param [String] view Name of view to render
  #   @param [Object] locals Context object to pass to the view
  #   @param [Object] headers Headers to send in the response
  #   @param [Integer] statusCode Response status code
  renderWithLayout: (layout, view, locals, headers, statusCode) ->
    try
      locals = merge context.toJSON(), locals
      body = renderView @appDir, view, locals
    catch ex
      @renderErrorWithLayout layout, ex, headers, statusCode
    @sendWithLayout layout, body, headers, statusCode

  # Render an error
  #
  # @overload renderError(error)
  #   @param [String] error Error text
  #   @param [Object] locals Context object to pass to the view
  # @overload renderError(error, headers)
  #   @param [String] error Error text
  #   @param [Object] headers Headers to send in the response
  # @overload renderError(error, headers, statusCode)
  #   @param [String] error Error text
  #   @param [Object] headers Headers to send in the response
  #   @param [Integer] statusCode Response status code
  renderError: (error, headers, statusCode) ->
    @renderErrorWithLayout "layout", error, headers, statusCode

  # Render an error with layout
  #
  # @overload renderError(layot, error)
  #   @param [String] layout Layout to use
  #   @param [String] error Error text
  # @overload renderError(layot, error, headers)
  #   @param [String] layout Layout to use
  #   @param [String] error Error text
  #   @param [Object] headers Headers to send in the response
  # @overload renderError(layot, error, headers, statusCode)
  #   @param [String] layout Layout to use
  #   @param [String] error Error text
  #   @param [Object] headers Headers to send in the response
  #   @param [Integer] statusCode Response status code
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

  # TODO what does this do?
  toJSON: ->
    @deferred.promise().claim()

