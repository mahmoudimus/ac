var requestContext = appContext.getBean("requestContext");
var signedRequestHandler = appContext.getBean("signedRequestHandler");

module.exports = {
  clientKey: function () {
    return requestContext.getClientKey();
  },
  userId: function () {
    return requestContext.getUserId();
  },
  hostBaseUrl: function () {
    return signedRequestHandler.getHostBaseUrl(this.clientKey());
  }
};
