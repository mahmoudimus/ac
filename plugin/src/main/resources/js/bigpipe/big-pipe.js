// This code makes a xhr request back to the server before dom ready looking for big pipe content to replace.
(function (global, AJS) {
  var $ = AJS.$;
  var AP = global._AP = global._AP || {};
  var contextPath = AJS.contextPath() || AJS.Meta.get("context-path");
  var requestId = $('meta[name="ap-request-id"]').attr("content");

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

  function replaceLoadingWithMessage() {
    $(document).ready(function() {
      $('.bp-loading').html("<img src='" + contextPath + "/download/resources/com.atlassian.labs.remoteapps-plugin:images/images/ajax-loader.gif' alt='loader'>");
    });
  }

  function replaceLoadingWithError(errType, status) {
    $(document).ready(function() {
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

  function poll() {
    if (!requestId) {
      AJS.log("Missing request id.  atl.header web panels not supported?");
      return;
    }
    $.ajax({
      url: contextPath + "/bigpipe/request/" + requestId,
      dataType: "json",
      timeout: 30000,
      success: function (response) {
        if (response.items && response.items.length !== 0) {
          $(function() {
            processContents(response);
            poll();
          });
        }
      },
      error: function(xhr, status, err) {
        replaceLoadingWithError(err, status);
      }
    });
  }
  poll();
  setTimeout(replaceLoadingWithMessage, 1000);

  AP.BigPipe = {
    processContents: processContents
  }

})(this, AJS);
