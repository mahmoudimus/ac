app = module.exports = require("atlassian/router").createApp("app")
app.configure require("./config")
require("./routes")(app)
