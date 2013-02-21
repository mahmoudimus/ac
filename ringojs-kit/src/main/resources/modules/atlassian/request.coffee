{mash, extend} = require "vendor/underscore"
{slurp} = require "atlassian/util"

module.exports = class Request

  parseUrlEncoded = do ->
    decode = decodeURIComponent
    add = (q, n, v) ->
      if not n then return
      dn = decode n
      dv = if v? then decode v else true
      if q[dn]
        if typeof q[dn] in ["string", "boolean"]
          q[dn] = [q[dn], dv]
        else
          q[dn].push dv
      else
        q[dn] = dv
    (str) ->
      query = {}
      add [query].concat(nv.split("="))... for nv in (str or "").split("&")
      query

  # Request
  # @param {object} request the jsgi request object
  constructor: (@request, options) ->

    # @todo from expressjs: is, accepts, accepted, ip, fresh, stale
    @path = @request.scriptName
    pathSplit = @path.indexOf("/", 1)
    @scriptName = @path.slice(0, pathSplit)
    @route = @pathInfo = @path.slice(pathSplit)
    @query = parseUrlEncoded @request.queryString
    @xhr = @request.isXhr
    @protocol = @request.scheme
    @secure = @protocol is "https"
    @headers = @request.headers
    @method = @request.method
    @env = @request.env
    @params = {}
    @host = @request.host
    @port = @request.port
    @version = @request.version
    @remoteAddress = @request.remoteAddress
    @scheme = @request.scheme

  # @property [String] request body
  body: ()->
    print("IN BODY")
    print("Content-Type: "+@header('content-type'))
    @body = do =>
      contentType = @header("content-type")
      charset = /;\s*charset=([^;]+)/.exec(contentType)?[1] or "latin1"
      body =
        if contentType?.indexOf("application/x-www-form-urlencoded") is 0
          jmap = @request.env.servletRequest.getParameterMap()
          mash ([e.key, e.value] for e in jmap.entrySet().toArray()).map ([k, v]) ->
            [k, if (v = if v.length is 1 then v[0] else v) is "" then true else v]
        else
          body = if @request.input?.inputStream then slurp(@request.input?.inputStream, charset) else undefined
          if contentType?.indexOf("application/json") is 0
            JSON.parse body
          else
            body
      body

  # returns a header value
  # @param [String] name of header
  header: (name) ->
    return v for k, v of @request.headers when k?.toLowerCase() is name?.toLowerCase()

  # alias for header
  get: (name)->
    @header(name)

  # returns a parameter value
  # @param [String] name of param
  @param: (name) ->
    @request.params?[name] or @body?[name] or @query?[name]

  # @property [Object] request object
  request: null

  # @property [String] path of request
  path: null

  # @property [String] script name
  scriptName: null

  # @property [String] route
  route: null

  # @property [String] alias for route
  pathInfo: null

  # @property [Object] params?
  params: null

  # @property [Array] query params
  query: null

  # @property [Boolean] request is xhr
  xhr: null

  # @property [String] request scheme
  protocol: null

  # @property [Boolean] request is SSL
  secure: null

  # @property [Array] request headers
  headers: null

  # @property [String] request method
  method: null

  # @property [Object] request environment
  env: null

  # @property [String] request host
  host: null

  # @property [String] request port
  port: null

  # @property [String] HTTP version
  version: null

  # @property [String] remote IP address
  remoteAddress: null

  # @property [String] HTTP scheme
  scheme: null


