http = require "atlassian/httpclient"
context = require "atlassian/context"
mustache = require "atlassian/renderer"

exports.app = (req) ->

  response = null
  error = null

  # make a test httpclient request
  http.get("/rest/remoteplugintest/1/user")
    .done((res) -> response = res)
    .fail((ex, exstr) -> error = exstr)
    .wait()

  # render the index view
  body = mustache.render "app/views/index.mustache",
    clientKey: context.clientKey()
    baseUrl: context.hostBaseUrl()
    hasHttpGetResponse: !!response
    httpGetStatus: response?.statusCode
    httpGetStatusText: response?.statusText
    httpGetContentType: response?.contentType
    httpGetEntity: response?.entity
    hasHttpGetError: !!error
    httpGetError: error

  # return the response
  status: 200
  headers: "Content-Type": "text/html"
  body: [body]
