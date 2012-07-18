var oauthContext = appContext.getBean("oauthContext");

exports.localBaseUrl = oauthContext.getLocalBaseUrl();

exports.getHostBaseUrl = function(key) {
  return oauthContext.getHostBaseUrl(key);
};
exports.validateRequest = function(jsgiReq) {
  var req = jsgiReq.env.servletRequest;
  return oauthContext.validateRequest(req);
};