function app(request) {
    if (request.pathInfo !== "/") {
      return {
        status: 404,
        headers: {
          "Content-Type": "text/plain"
        },
        body: ["Page not found."]
      };
    }

    return {
      status: 200,
      headers: {
        "Content-Type": "text/plain"
      },
      body: ["Hello World!"]
    };
  };

exports.app = app;
