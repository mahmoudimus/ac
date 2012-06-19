(function (global, $) {

    var xhrProperties = ["status", "statusText", "responseText"],
        xhrHeaders = ["Content-Type"],
        RA = global.RemoteApps;

    RA.create = RA.create || function (options) {
        var ns = options.ns,
            containerId = "embedded-" + ns,
            channelId = "channel-" + ns,
            container$ = $("#" + containerId),
            initHeight = options.height || "10em",
            initWidth = options.width || "100%",
            now = new Date().getTime();

        var rpc = new easyXDM.Rpc({
            remote: options.src,
            container: containerId,
            channel: channelId,
            protocol: options.protocol,
            props: {height: initHeight, width: initWidth}
        }, {
            remote: {
                dialogMessage: {}
            },
            local: {
                init: function () {
                    container$.addClass("iframe-init");
                    $("#ra-time-" + ns).text(new Date().getTime() - now);
                },
                resize: function (width, height) {
                    $("iframe", container$).css({height: height, width: width});
                },
                getLocation: function () {
                    return global.location.href;
                },
                getUser: function () {
                    // JIRA 5.0, Confluence 4.3(?)
                    var meta = AJS.Meta,
                        fullName = meta ? meta.get("remote-user-fullname") : null;
                    if (!fullName) {
                        // JIRA 4.4, Confluence 4.1, Refapp 2.15.0
                        fullName = $("a#header-details-user-fullname, .user.ajs-menu-title, a#user").text();
                    }
                    return {fullName: fullName, id: options.userId};
                },
                showMessage: function (id, title, body) {
                    // init message bar if necessary
                    if ($("#aui-message-bar").length === 0) {
                        // TODO - adding #aui-message-bar only works for the view-issue page for now.
                        $.html("div").attr("id", "aui-message-bar").prependTo("#details-module");
                    }
                    $(".aui-message#" + id).remove();
                    AJS.messages.info({
                        title: title,
                        body: "<p>" + $("<div/>").text(body).html() + "<p>",
                        id: id,
                        closeable: false
                    });
                },
                clearMessage: function (id) {
                    $(".aui-message#" + id).remove();
                },
                request: function (args, success, error) {
                    // add the context path to the request url
                    var url = options.contextPath + args.url;
                    // reduce the xhr object to the just bits we can/want to expose over the bridge
                    function toJSON(xhr) {
                        var json = {headers: {}};
                        // only copy key properties and headers for transport across the bridge
                        $.each(xhrProperties, function (i, v) { json[v] = xhr[v]; });
                        // only copy key response headers for transport across the bridge
                        $.each(xhrHeaders, function (i, v) { json.headers[v] = xhr.getResponseHeader(v); });
                        return json;
                    }
                    function done(data, textStatus, xhr) { success([data, textStatus, toJSON(xhr)]); }
                    function fail(xhr, textStatus, errorThrown) { error([toJSON(xhr), textStatus, errorThrown]); }
                    // execute the request with our restricted set of inputs
                    $.ajax({
                        url: url,
                        type: args.type || "GET",
                        data: args.data,
                        dataType: "text", // prevent jquery from parsing the response body
                        contentType: args.contentType,
                        headers: {
                            // undo the effect on the accept header of having set dataType to "text"
                            "Accept": "*/*",
                            // send the app key header to force scope checks
                            "RA-App-Key": options.appKey
                        }
                    }).then(done, fail);
                }
            }
        });

        var placeholderContainer$ = container$.parents(".ra-servlet-placeholder");

        placeholderContainer$.bind("ra.dialog.submit", function (e, callback) {
            rpc.dialogMessage("submit", callback);
        });

        placeholderContainer$.bind("ra.dialog.cancel", function (e, callback) {
            rpc.dialogMessage("cancel", callback);
        });

        placeholderContainer$.bind("ra.iframe.destroy", function () {
            rpc.destroy();
        });

    }

}(this, AJS.$));
