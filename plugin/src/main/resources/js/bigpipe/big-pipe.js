/*
This code makes a xhr request back to the server before dom ready looking for big pipe content to
replace.
 */
(function ($, global) {
  var RemoteApps = global.RemoteApps = global.RemoteApps || {};
  var contextPath = AJS.contextPath() || AJS.Meta.get("context-path");
  var requestId = $('meta[name="ra-request-id"]').attr("content");

  function insertContent(content) {
    var contentId = content.id;
    var html = content.html;
    var contentDiv$ = $('.bp-' + contentId);
    if (contentDiv$.length == 0) {
      if (content.html) {
        $("body").append(content.html);
      }
    } else {
      contentDiv$.html(html);
      contentDiv$.removeClass("bp-loading");
    }
  }

  function insertContents(contents){
    if (contents.length > 0) {
      $.each(contents, function () {
        insertContent(this);
      });
    }
  }

  function replaceLoadingWithMessage() {
    $(document).ready(function() {
      $('.bp-loading').html("<img src='" + contextPath + "/download/resources/com.atlassian.labs.remoteapps-plugin:images/images/ajax-loader.gif' alt='loader'>");
    });
  }

  function replaceLoadingWithError(errType, status) {
    $(document).ready(function() {
      $('.bp-loading').html("<div>Error: " + status + "</div>");
    });
  }

  function poll() {
    if (!requestId) {
      AJS.log("Missing request id.  atl.header web panels not supported?");
      return;
    }
    $.ajax({ url:contextPath + "/rest/remoteapps/latest/bigpipe/request/" + requestId,
      success:function (data) {
        if (data.length != 0) {
          $(document).ready(function() {
            insertContents(data);
            poll();
          });
        } else {
          replaceLoadingWithError("missing", "Missing content");
        }
      },
      error:function(xhr, status, err) {
        replaceLoadingWithError(err, status);
      }, dataType:"json", timeout:30000 });
  }
  poll();
  setTimeout(replaceLoadingWithMessage, 1000);

  RemoteApps.BigPipe = {
    insertContents : insertContents
  }
})(AJS.$, this);