AP.define("cookie", ["_dollar", "_rpc"],

/**
* Allows cookies to be stored / retrieved and erased. This evades the restrictions on 3rd party cookies.
* @exports cookie
*/

function ($, rpc) {
    "use strict";

    var exports;

    rpc.extend(function (remote) {
        exports = {

            /**
            * Save a cookie.
            * @param name {String} name of cookie
            * @param value {String} value of cookie
            * @param expires {Number} number of days before cookie expires
            */
            save:function(name, value, expires){
                remote.saveCookie(name, value, expires);
            },

            /**
            * Get the value of a cookie.
            * @param name {String} name of cookie to read
            * @param callback {Function} callback to pass cookie data
            */
            read:function(name, callback){
                remote.readCookie(name, callback);
            },

            /**
            * Remove the given cookie.
            * @param name {String} the name of the cookie to remove
            */
            erase:function(name){
                remote.eraseCookie(name);
            }
        };
        return {
            stubs: ['saveCookie', 'readCookie', 'eraseCookie']
        };
    });

    return exports;

});
