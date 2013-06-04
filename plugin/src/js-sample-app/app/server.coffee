http = require "atlassian/http/host-client"
context = require "atlassian/context"
{renderTemplate} = require "atlassian/util"

exports.app = (req) ->

  # make a test httpclient request
  try
    response = http.newRequest("/rest/remoteplugintest/1/user").get().claim()
  catch ex
    error = ex


  if context.clientKey == null
    return {
      status: 404
      headers: "Content-Type": "text/plain"
      body: ["Must be authenticated"]
    }

  # render the index view
  body = renderTemplate "app/views/index",
    clientKey: context.clientKey
    localBaseUrl: context.hostBaseUrl
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
