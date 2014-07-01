_AP.define("host/cookie", ["_dollar", "_rpc"], function ($, rpc) {
    "use strict";

    var module;

    rpc.extend(function () {
        function prefixCookie(addonKey, name){
            if (!(addonKey && addonKey.length > 0)){
                throw new Error('addon key must be defined on cookies');
            }
            if(name.length < 1){
                throw new Error('Name must be defined');
            }
            return addonKey + '$$' + name;
        }

        module = {
            internals: {
                saveCookie: function(name, value, expires){
                    AJS.Cookie.save(prefixCookie(this.addonKey, name), value, expires);
                },
                readCookie: function(name, callback){
                    var value = AJS.Cookie.read(prefixCookie(this.addonKey, name));
                    if(typeof callback === "function"){
                        callback(value);
                    }
                },
                eraseCookie: function(name){
                     AJS.Cookie.erase(prefixCookie(this.addonKey, name));
                }
            }
        };
        return module;
    });

    return module;
});
