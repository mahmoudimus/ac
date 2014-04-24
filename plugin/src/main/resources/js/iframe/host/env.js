_AP.define("env", ["_dollar", "_rpc"], function ($, rpc) {
    "use strict";
    var timeZone;

    rpc.extend(function(config){
        return {
            init: function(state){
                timeZone = state.data.timeZone;
            },
            internals: {
                getLocation: function () {
                    return window.location.href;
                },
                getUser: function () {
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
                    return {fullName: fullName, id: config.uid, key: config.ukey};
                },
                getTimeZone: function () {
                    return timeZone;
                }
            }
        };
    });

});
