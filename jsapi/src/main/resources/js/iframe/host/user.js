connectHost.defineModule("user", {
    getUser: function (callback) {
      var data = callback._context.extension.options.user;
        // JIRA 5.0, Confluence 4.3(?)
        var meta = AJS.Meta,
        fullName = meta ? meta.get("remote-user-fullname") : null;
        if (!fullName) {
            // JIRA 4.4, Confluence 4.1, Refapp 2.15.0
            fullName = $("a#header-details-user-fullname, .user.ajs-menu-title, a#user").text();
        }
        if (!fullName) {
            // JIRA 6, Confluence 5
            fullName = $("a#user-menu-link").attr("title");
        }
        callback({fullName: fullName, id: data.uid, key: data.ukey});
    },
    getTimeZone: function (callback) {
        callback(callback._context.extension.options.user.timeZone);
    }
});