{slurp} = require "atlassian/util"

module.exports = (app) ->

  app.get "/general", (req, res) ->
    res.render "general",
      headers: ({name, value} for name, value of req.headers)
      pathParams: ({name, value} for name, value of req.params)
      queryParams: ({name, value} for name, value of req.query)
      method: req.method
      scriptName: req.scriptName
      pathInfo: req.pathInfo
      queryString: req.queryString
      host: req.host
      port: req.port
      scheme: req.scheme
      body: JSON.stringify(req.body, null, 2)
