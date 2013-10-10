// This code makes a xhr request back to the server before dom ready looking for big pipe content to replace.
// TODO: naming here (bigpipe/bigpipe) is probably stupid
_AP.define("bigpipe/bigpipe", ["_dollar"], function($) {

  var contextPath = AJS.contextPath() || AJS.Meta.get("context-path");
  var counter = 0;

  // bodyHtml is the content returned from the big pipe execution. ChannelId may be 'html' or 'script'
  function insertContent(channelId, contentId, bodyHtml) {
    var contentDiv$ = $('#' + contentId);
    if (contentDiv$.length == 0) {
      // Only append 'script' channel types to the bottom of the page
      if (bodyHtml && channelId === 'script') {
        $("body").append(bodyHtml);
      }
    } else {
      contentDiv$.html(bodyHtml).removeClass("bp-loading");
    }
  }

  function displayLoadingSpinner() {
    $(function() {
      $('.bp-loading').html("<img src='" + contextPath + "/download/resources/com.atlassian.plugins.atlassian-connect-plugin:images/images/ajax-loader.gif' alt='loader'>");
    });
  }

  function replaceLoadingWithError(errType, status) {
    $(function() {
      $('.bp-loading').html("<div>Error: " + status + "</div>").removeClass("bp-loading");
    });
  }

  function processContents(contents) {
    if (contents.items && contents.items.length > 0) {
      $.each(contents.items, function () {
        if ($.inArray(this.channelId, ["html", "script"]) > -1 && this.content) {
          insertContent(this.channelId, this.contentId, this.content);
        }
      });
    }
  }

  function poll(requestId) {
    $.ajax({
      url: contextPath + "/bigpipe/request/" + requestId + "/" + (counter += 1),
      dataType: "json",
      timeout: 30000,
      success: function (response) {
        if (response.items && response.items.length !== 0) {
          if (response.pending.length > 0) {
            poll(requestId);
          }
          $(function() { processContents(response); });
        }
      },
      error: function(xhr, status, err) {
        replaceLoadingWithError(err, status);
      }
    });
  }

  var isStarted;
  return {
    start: function (options) {
      if (!isStarted) {
        isStarted = true;
        if (options.ready) processContents(options.ready);
        if (!options.ready || options.ready.pending.length > 0) {
          poll(options.requestId);
          setTimeout(displayLoadingSpinner, 1000);
        }
      }
    }
  };

});

/**
 * Legacy namespace
 * @deprecated
 */
if (!_AP.BigPipe) {
  _AP.require(["bigpipe/bigpipe"], function(main) {
    _AP.BigPipe = main;
  });
}
