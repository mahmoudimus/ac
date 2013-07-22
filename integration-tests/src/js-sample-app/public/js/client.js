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
  // rnd param assures cachebusting
  RA.request("/rest/remoteplugintest/1/user?rnd=" + Math.random(), {
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
  RA.request("/rest/remoteplugintest/1/user?rnd=" + Math.random(), {
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
  RA.request("/rest/remoteplugintest/1/user?rnd=" + Math.random(), {
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
