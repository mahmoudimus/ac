_AP.define("messages-rpc", ["_dollar", "messages/main", "_rpc"], function($, messages, rpc) {
    rpc.extend(function () {
        return {
            internals: {
                showMessage: function (name, title, body, options) {
                    return messages.showMessage(name, title, body, options);
                },
                clearMessage: function (id) {
                    return messages.clearMessage(id);
                },
            }
        };
    });

});
