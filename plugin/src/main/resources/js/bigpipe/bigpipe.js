// This code makes a xhr request back to the server before dom ready looking for big pipe content to replace.
// TODO: naming here (bigpipe/bigpipe) is probably stupid
_AP.define("bigpipe/bigpipe", ["_dollar"], function($) {

  var contextPath = AJS.contextPath() || AJS.Meta.get("context-path");
  var counter = 0;

  function insertContent(contentId, html) {
    var contentDiv$ = $('#' + contentId);
    if (contentDiv$.length == 0) {
      if (html) {
        $("body").append(html);
      }
    } else {
      contentDiv$.html(html).removeClass("bp-loading");
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
        if (this.channelId === "html" && this.content) {
          insertContent(this.contentId, this.content);
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
          poll(requestId);
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