/*
This code makes a xhr request back to the server before dom ready looking for big pipe content to
replace.
 */
(function ($) {
  var contextPath = AJS.Meta.get("context-path");
  var requestId = $('meta[name="ra-request-id"]').attr("content");

  function insertContent(content) {
    var contentId = content.id;
    var html = content.html;
    var contentDiv$ = $('#' + contentId);
    contentDiv$.html(html);
    contentDiv$.removeClass("bp-loading");
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
    $.ajax({ url:contextPath + "/rest/remoteapps/latest/bigpipe/request/" + requestId,
      success:function (data) {
        if (data.length != 0) {
          $(document).ready(function() {
            $.each(data, function () {
              insertContent(this);
            });
          });
          poll();
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
})(AJS.$);