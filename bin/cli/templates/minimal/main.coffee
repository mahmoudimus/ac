app = exports.app = require("atlassian/router").createApp()

app.get "/general", (req, res) ->
  res.render "general",
    message: "Hello World"
