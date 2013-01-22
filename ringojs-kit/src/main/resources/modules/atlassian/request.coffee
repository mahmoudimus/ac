{mash, extend} = require "vendor/underscore"
{slurp} = require "atlassian/util"

properties = ["headers", "method", "env", "host", "port", "version", "remoteAddress", "scheme"]

module.exports = (req, options) ->

  path = req.scriptName
  pathSplit = path.indexOf "/", 1
  scriptName = path.slice(0, pathSplit)
  route = pathInfo = path.slice(pathSplit)
  header = get = (name) ->
    return v for k, v of req.headers when k?.toLowerCase() is name?.toLowerCase()
  params = {}
  param = (name) ->
    req.params?[name] or @body?[name] or query?[name]
  query = parseUrlEncoded req.queryString
  xhr = req.isXhr
  protocol = req.scheme
  secure = protocol is "https"
  request = req

  # @todo from expressjs: is, accepts, accepted, ip, fresh, stale

  self = {path, scriptName, route, pathInfo, header, get, params, param, query, xhr, protocol, secure, request}

  self.__defineGetter__ "body", ->
    @_body ?= do ->
      contentType = header("content-type")
      charset = /;\s*charset=([^;]+)/.exec(contentType)?[1] or "latin1"
      body =
        if contentType?.indexOf("application/x-www-form-urlencoded") is 0
          jmap = req.env.servletRequest.getParameterMap()
          mash ([e.key, e.value] for e in jmap.entrySet().toArray()).map ([k, v]) ->
            [k, if (v = if v.length is 1 then v[0] else v) is "" then true else v]
        else
          body = if req.input?.inputStream then slurp(req.input?.inputStream, charset) else undefined
          if contentType?.indexOf("application/json") is 0
            JSON.parse body
          else
            body
      body

  extend self, mash([k, v] for k, v of req when k in properties)

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
