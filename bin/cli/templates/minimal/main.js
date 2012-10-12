var app = exports.app = require("atlassian/router").createApp();

app.get("/general", function (req, res) {
  res.render("general", {
    message: "Hello World"
  });
});
