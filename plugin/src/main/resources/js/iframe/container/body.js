(function (global, $) {

    global.RemoteApps = $.extend(RemoteApps || {}, {

        create: function (options) {
            var ns = options.ns;
            var src = options.src;
            var containerId = "embedded-" + ns;
            var channelId = "channel-" + ns;
            var container$ = $("#" + containerId);
            var initHeight = options.height || "10em";
            var initWidth = options.width || "100%";
            var protocol = options.protocol;
            var now = new Date().getTime();

            var rpc = new easyXDM.Rpc({
                remote: src,
                container: containerId,
                channel: channelId,
                protocol: protocol,
                props: {height: initHeight, width: initWidth}
            }, {
                remote: {
                    dialogMessage: {}
                },
                local: {
                    init: function () {
                        container$.addClass("iframe-init");
                        $("#ra-time-" + options.ns).text(new Date().getTime() - now);
                    },
                    resize: function (height, width) {
                        $("iframe", container$).css({height: height, width: width});
                    },
                    getLocation: function () {
                        return global.location.href;
                    },
                    getUser: function () {
                        // JIRA 5.0
                        var fullName = $("meta[name=ajs-remote-user-fullname]").attr("content");
                        if (!fullName) {
                            // JIRA 4.4, Confluence 4.1, Refapp 2.15.0
                            fullName = $("a#header-details-user-fullname, .user.ajs-menu-title, a#user").text();
                        }
                        return {fullName: fullName};
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

    });

}(this, AJS.$));
