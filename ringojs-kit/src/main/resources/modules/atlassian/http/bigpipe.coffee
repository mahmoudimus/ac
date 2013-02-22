{proxy} = require "atlassian/util"

# BigPipe provides an API to allow non-blocking page load for
# situations where you need to execute a blocking operation,
# such as http calls.
#
# ## Example
#
# The following is an example of a simple plugin registered at `/index`
# that retrieves lorem ipsum data from a remote service 4 times, then
# returns each response asyncronously through BigPipe.
#
#     let app = exports.app = require("atlassian/app").create();
#
#     let http = require("atlassian/http/client");
#     let bigpipe = require("atlassian/http/bigpipe");
#     let {range} = require("vendor/underscore");
#
#     app.get("/index", function (req, res) {
#       res.render("index", {
#         loripsums: range(4).map(function (i) {
#           let promise =
#             http.newRequest("http://loripsum.net/api").get()
#               .transform()
#               .ok(function (response) response.getEntity())
#               .otherwise(function (t) "<pre>" + t + "</pre>")
#               .toPromise();
#           return {
#             html: bigpipe.promiseHtmlContent(promise).initialContent,
#             index: i + 1
#           };
#         })
#       });
#     });
#
# The corresponding view `index.hbs` referenced in the `render` method above:
#
#     <div class="aui-group">
#     {{#each loripsums}}
#       <div class="aui-item">
#         <h3>Random #{{index}}</h3>
#         {{{html}}}
#       </div>
#     {{/each}}
#     </div>
#
# @see https://remoteapps.jira.com/wiki/display/ARA/Using+BigPipe+in+P3+plugins
# @see https://bitbucket.org/rbergman/xhr-bigpipe-plugin
# @see https://bitbucket.org/rbergman/loripsum-plugin
class BigPipe
  # No need to create a BigPipe instance. This module creates a new instance when required.
  constructor: ->
    return appContext.getBean("bigPipe")

  # The following methods/properties are just used by the documentation generator

  # The thread-local request id
  # @return [Integer] The thread-local request id
  getRequestId: ->

  # @property [Integer] The thread-local request id
  requestId: null

  # Whether or not any content has been promised to this BigPipe instance for the current request
  # @return [Boolean]
  isActivated: ->

  # Registers an HTML content promise on the HTML channel
  #
  # @param [Promise<String>] contentPromise A promise for the HTML content string
  # @return [HtmlPromise] An HtmlPromise that can be used to access an injectable HTML string that serves either as
  # a placeholder for future content injection, or the content itself if the promise has already
  # been resolved
  promiseHtmlContent: (contentPromise)->

  # Registers a general string promise on the named channel.  Use of this method will generate client-side BigPipe
  # events delivering the promised strings to subscribers by channelId.

  # @param [String] channelId The channelId on which to promise the content string
  # @param [Promise<String>] contentPromise A promise for the content string
  # @throw [IllegalArgumentException] if the channelId is BigPipe#HTML_CHANNEL_ID
  promiseContent: (channelId, contentPromise)->

  # Consume all completed content without blocking, to be called on the same request that populated BigPipe with
  # calls to promiseHtmlContent(Promise) or promiseContent(String, Promise).
  #
  # @return [String] A JSON array of all content from promises that have been fulfilled at the time of the call
  consumeContent: ->


module.exports = new BigPipe