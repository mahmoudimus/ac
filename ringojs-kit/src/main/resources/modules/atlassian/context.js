var requestContext = appContext.getBean("requestContext");

module.exports = {
  clientKey: function () {
    return requestContext.getClientKey();
  },
  userId: function () {
    return requestContext.getUserId();
  },
  hostBaseUrl: function () {
    return requestContext.getHostBaseUrl();
  }
};
