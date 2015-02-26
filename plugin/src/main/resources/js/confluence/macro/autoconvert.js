(function () {
    AJS.bind("init.rte", function () {
        function connectPasteHandler(uri, node, done) {
            // the contents of autoConvertMappings will be generated
            var macroName = "google-drive";
            var autoConvertMappings = [
                {
                    "pattern": /docs.google.com\/.*\/document\/d\/(.*?)\/edit.*/,
                    "parameters": ["fileId"]
                },
                {
                    "pattern": /docs.google.com\/.*\/spreadsheets\/d\/(.*?)\/edit.*/,
                    "parameters": ["fileId"]
                },
                {
                    "pattern": /docs.google.com\/.*\/presentation\/d\/(.*?)\/edit.*/,
                    "parameters": ["fileId"]
                }
            ];

            for (var i = 0; i < autoConvertMappings.length; i++) {
                var matches = uri.source.match(autoConvertMappings[i]["pattern"]);

                if (matches) {
                    var params = {};
                    for (var j = 0; j < autoConvertMappings[i].parameters.length; j++) {
                        params[autoConvertMappings[i].parameters[j]] = matches[j+1];
                    }
                    var macro = {
                        name: macroName,
                        params: params
                    };
                    tinymce.plugins.Autoconvert.convertMacroToDom(macro, done, done);
                    return;
                }
            }
            done();
        }

        tinymce.plugins.Autoconvert.autoConvert.addHandler(connectPasteHandler);
    });
})();
