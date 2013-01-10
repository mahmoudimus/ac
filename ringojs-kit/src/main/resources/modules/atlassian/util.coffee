importClass com.atlassian.plugin.util.PluginUtils
importClass org.apache.commons.io.IOUtils
pluginRetrievalService = appContext.getService "com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService"
signedRequestHandler = appContext.getBean "signedRequestHandler"

{normal} = require "fs"
{merge, mash, extend} = require "vendor/underscore"

devMode = java.lang.Boolean.getBoolean PluginUtils.ATLASSIAN_DEV_MODE

exports = module.exports =

  # tests whether the app is running in dev mode
  devMode: devMode

  plugin: ->
    pluginRetrievalService.getPlugin()

  # creates a js proxy object for for the given java 'delegate'
  proxy: (delegate, mixin) ->
    proxy = Object.create delegate
    name = (GorS) -> "__define#{GorS}etter__"
    etter = (GorS) -> (p, fn) -> new Object()[name(GorS)].call(@, p, fn)
    Object.defineProperty proxy, name(GorS), {value: etter(GorS)} for GorS in ["G", "S"]
    extend proxy, mixin

  slurp: (stream, charset="UTF-8") ->
    throw new Error "Invalid input stream '#{stream}'" if not stream
    IOUtils.toString stream, charset

  publicUrl: (type, path) ->
    exts = [".min.#{type}", "-min.#{type}"]
    exts[if devMode then "unshift" else "push"] ".#{type}"
    for ext in exts
      file = exports.resourcePath "public/#{type}/#{path}", ext
      return file if file
    throw new Error "No #{type} resource found for path '#{path}'"

  resourcePath: (base, ext) ->
    ext = "" if base.indexOf(ext) is base.length - ext.length
    if exports.plugin().getResource(path = normal(base + ext)) then path else null

  renderTemplate: do ->

    Handlebars = do ->
      resources = {}
      templates = {}
      cache = (store, key, generator) ->
        if devMode then generator() else store[key] ?= generator()
      realPath = (path, relativeTo) ->
        if relativeTo
          normal "#{relativeTo}/#{if path.indexOf('../') is 0 then '' else '../'}#{path}"
        else
          path
      read = (path) ->
        cache resources, path, ->
          resource = exports.plugin().getResourceAsStream(path)
          throw new Error "Resource not found for path '#{path}'" if not resource
          exports.slurp resource
      hbs = require("vendor/handlebars").Handlebars
      hbs.registerHelper "include", (path) -> new hbs.SafeString read(realPath(path, @_path))
      #hbs.registerHelper "partial", (path) -> new hbs.SafeString hbs.render(path, @)
      hbs.render = (path, locals) ->
        path = realPath path, locals._path
        template = cache templates, path, -> hbs.compile(read path)
        template merge locals, _path: path
      hbs.resolvePath = (path) ->
        (exports.resourcePath path, ext for ext in [".handlebars", ".hbs"]).filter((p) -> !!p)[0]
      hbs

    nativeRenderer = appContext.getBean "templateRenderer"

    (view, locals) ->
      if (hbsPath = Handlebars.resolvePath view)
        Handlebars.render hbsPath, locals
      else if nativeRenderer.canRender view
        nativeRenderer.render view, locals
      else
        null

  hostBaseUrlFor: (clientKey) ->
    signedRequestHandler.getHostBaseUrl clientKey
