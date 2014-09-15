AP.define("user", ["_dollar", "_rpc"], function ($, rpc) {
    "use strict";

    var apis = rpc.extend(function (remote) {
        return {
            apis: {
                /**
                * get a user object containing the user's id and full name
                *
                * @param {Function} callback  function (user) {...}
                * @example
                * AP.getUser(function(user){ 
                *   console.log(user);
                * });
                */
                getUser: function (callback) {
                    remote.getUser(callback);
                },
                /**
                * get current timezone - if user is logged in then this will retrieve user's timezone
                * the default (application/server) timezone will be used for unauthorized user
                *
                * @param {Function} callback  function (user) {...}
                * @example
                * AP.getTimeZone(function(timezone){
                *   console.log(timezone);
                * });
                */
                getTimeZone: function (callback) {
                  remote.getTimeZone(callback);
                }
            }
        };
    });

    // backwards compatability.
    AP.getUser = apis.getUser;
    AP.getTimeZone = apis.getTimeZone;

    return apis;
});
