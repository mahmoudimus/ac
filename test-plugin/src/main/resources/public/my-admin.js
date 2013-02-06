(function ($, AP) {
  // general api testing
  AP.getUser(function(user) {
    $("#user").text(user.fullName);
    $("#userId").text(user.id);
  });

  AP.getTimeZone(function(timeZone) {
    $("#timeZone").text(timeZone);
  });

  AP.getLocation(function(location) {
    $("#location").text(location);
  });

  AP.fireEvent("testevent", {});

  // basic request api testing
  function bindXhr(xhr) {
    $("#client-http-status").text(xhr.status);
    $("#client-http-status-text").text(xhr.statusText);
    $("#client-http-content-type").text(xhr.getResponseHeader("content-type"));
    $("#client-http-response-text").text(xhr.responseText);
  }
  AP.request("/rest/remoteplugintest/1/user", {
    success: function (data, statusText, xhr) {
      $("#client-http-data").text(data);
      bindXhr(xhr);
    },
    error: function (xhr) {
      bindXhr(xhr);
    }
  });
}(jQuery, AP));
