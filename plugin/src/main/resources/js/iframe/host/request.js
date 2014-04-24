_AP.define("request", ["_dollar", "_rpc"], function ($, rpc) {
    "use strict";

    var xhrProperties = ["status", "statusText", "responseText"],
        xhrHeaders = ["Content-Type"];

    rpc.extend(function(config){
        return {
            internals: {
                request: function(args, success, error){
                    // add the context path to the request url
                    var url = AJS.contextPath() + args.url;
                    // reduce the xhr object to the just bits we can/want to expose over the bridge
                    function toJSON(xhr) {
                        var json = {headers: {}};
                        // only copy key properties and headers for transport across the bridge
                        $.each(xhrProperties, function (i, v) { json[v] = xhr[v]; });
                        // only copy key response headers for transport across the bridge
                        $.each(xhrHeaders, function (i, v) { json.headers[v] = xhr.getResponseHeader(v); });
                        return json;
                    }
                    function done(data, textStatus, xhr) {
                        success([data, textStatus, toJSON(xhr)]);
                    }
                    function fail(xhr, textStatus, errorThrown) {
                        error([toJSON(xhr), textStatus, errorThrown]);
                    }

                    var headers = {};
                    $.each(args.headers || {}, function (k, v) { headers[k.toLowerCase()] = v; });
                    // Disable system ajax settings. This stops confluence mobile from injecting callbacks and then throwing exceptions.
                    $.ajaxSettings = {};

                    // execute the request with our restricted set of inputs
                    $.ajax({
                        url: url,
                        type: args.type || "GET",
                        data: args.data,
                        dataType: "text", // prevent jquery from parsing the response body
                        contentType: args.contentType,
                        headers: {
                            // */* will undo the effect on the accept header of having set dataType to "text"
                            "Accept": headers.accept || "*/*",
                            // send the client key header to force scope checks
                            "AP-Client-Key": config.addonKey
                        }
                    }).then(done, fail);
                }

            }
        };
    });

});
