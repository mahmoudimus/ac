(this.AP || this._AP).define("_ui-params", ["_dollar", "_base64", "_uri"], function($, base64, Uri) {


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
        * Decode a base64 encoded json string containing ui params
        */
        decode: function(params){
            if(params && params.length > 0){
                return JSON.parse(base64.decode(params));
            }
            return {};
        }
    }

});
