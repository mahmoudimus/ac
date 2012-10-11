(function ($, RA) {
  // general api testing
  RA.getUser(function(user) {
    $("#user").text(user.fullName);
    $("#userId").text(user.id);
  });

  RA.getLocation(function(location) {
    $("#location").text(location);
  });

  // basic request api testing
  function bindXhr(xhr) {
    $("#client-http-status").text(xhr.status);
    $("#client-http-status-text").text(xhr.statusText);
    $("#client-http-content-type").text(xhr.getResponseHeader("content-type"));
    $("#client-http-response-text").text(xhr.responseText);
  }
  RA.request("/rest/remoteplugintest/1/user", {
    success: function (data, statusText, xhr) {
      $("#client-http-data").text(data);
      bindXhr(xhr);
    },
    error: function (xhr) {
      bindXhr(xhr);
    }
  });
}(jQuery, RA));
