# Ringo-Kit

## Sample plugin using Ringo-Kit

    let app = exports.app = require("atlassian/app").create();

    app.configure({
      stylesheets: ["app"],
      scripts: ["app"]
    });

    app.all("/index", function (req, res) {
      res.sendText('Hello World!');
    });
