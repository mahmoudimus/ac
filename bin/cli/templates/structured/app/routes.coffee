module.exports = (app) ->

  app.get "/general", (req, res) ->
    res.render "general",
      headers: ({name: k, value: v} for k, v of req.headers)
      method: req.method
      scriptName: req.scriptName
      pathInfo: req.pathInfo
      queryString: req.queryString
      host: req.host
      port: req.port
      scheme: req.scheme
      input: req.input
