(this.AP || this._AP).define("_ui-params", ["_dollar", "_base64", "_uri"], function($, base64, Uri) {

    /**
    * These are passed into the main host create statement and can override
    * any options inside the velocity template.
    * Additionally these are accessed by the js inside the client iframe to check if we are in a dialog.
    */

    return {
        /**
        * Encode options for transport
        */
        encode: function(options){
            return base64.encode(JSON.stringify(options));
        },
        /**
        * return ui params from a Url
        **/
        fromUrl: function(url){
            var url = new Uri.init(url),
            params = url.getQueryParamValue('ui-params');
            return this.decode(params);
        },
        /**
        * returns ui params from window.name
        */
        fromWindowName: function(w, param){
            w = w || window;
            var decoded = this.decode(w.name);

            if(!param){
                return decoded;
            }
            return (decoded) ? decoded[param] : undefined;
        },
        /**
        * Decode a base64 encoded json string containing ui params
        */
        decode: function(params){
            var obj = {};
            if(params && params.length > 0){
                try {
                    obj = JSON.parse(base64.decode(params));
                } catch(e) {
                    if(console && console.log){
                        console.log("Cannot decode passed ui params", params);
                    }
                }
            }
            return obj;
        }
    };

});
