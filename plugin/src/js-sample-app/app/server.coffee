{all} = require "atlassian/deferred"
http = require "atlassian/httpclient"
context = require "atlassian/context"
mustache = require "atlassian/renderer"

exports.app = (req) ->

  response = null
  error = null

  # make a test httpclient request
  http.get("/rest/remoteapptest/1/user")
    .done((res) -> response = res)
    .fail((ex, exstr) -> error = exstr)
    .wait()

  # foo = http.get("/foo")
  # bar = http.get("/bar")
  # all(foo, bar)
  #   .done((res1, res2) -> console.log JSON.stringify(res1, null, 2), JSON.stringify(res2, null, 2))
  #   .fail((ex, exstr) -> console.error exstr)
  #   .wait()

  # render the index view
  body = mustache.render "app/views/index.mustache",
    clientKey: context.clientKey()
    baseUrl: context.hostBaseUrl()
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
