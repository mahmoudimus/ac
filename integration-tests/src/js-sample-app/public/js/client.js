(function () {
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
    headers: {
      "Accept": "text/plain"
    },
    success: function (data, statusText, xhr) {
      $("#client-http-data").text(data);
      bindXhr(xhr);
      RA.resize();
    },
    error: function (xhr, statusText, errorThrown) {
      bindXhr(xhr);
      RA.resize();
    }
  });
  // additional media type requests
  RA.request("/rest/remoteplugintest/1/user", {
    headers: {
      "Accept": "application/json"
    },
    success: function (data, statusText, xhr) {
      $("#client-http-data-json").text(data);
      RA.resize();
    },
    error: function (xhr, statusText, errorThrown) {
      console.error(xhr, statusText, errorThrown);
      RA.resize();
    }
  });
  RA.request("/rest/remoteplugintest/1/user", {
    headers: {
      "Accept": "application/xml"
    },
    success: function (data, statusText, xhr) {
      $("#client-http-data-xml").text(data);
      RA.resize();
    },
    error: function (xhr, statusText, errorThrown) {
      console.error(xhr, statusText, errorThrown);
      RA.resize();
    }
  });
}());
