var {slurp} = require("atlassian/util");

module.exports = function (app) {

  app.get("/general", function (req, res) {
    var headers = [], pathParams = [], queryParams = [];
    for (var k in req.headers) headers.push({name: k, value: req.headers[k]});
    for (var k in req.params) pathParams.push({name: k, value: req.params[k]});
    for (var k in req.query) queryParams.push({name: k, value: req.query[k]});
    res.render("general", {
      headers: headers,
      pathParams: pathParams,
      queryParams: queryParams,
      method: req.method,
      scriptName: req.scriptName,
      pathInfo: req.pathInfo,
      queryString: req.queryString,
      host: req.host,
      port: req.port,
      scheme: req.scheme,
      body: JSON.stringify(req.body, null, 2)
    });
  });

};
