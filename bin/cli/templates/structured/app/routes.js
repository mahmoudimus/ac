module.exports = function (app) {

  app.get("/general", function (req, res) {
    var headers = [];
    for (var k in req.headers) headers.push({name: k, value: req.headers[k]});
    res.render("general", {
      headers: headers,
      method: req.method,
      scriptName: req.scriptName,
      pathInfo: req.pathInfo,
      queryString: req.queryString,
      host: req.host,
      port: req.port,
      scheme: req.scheme,
      input: req.input
    });
  });

};
