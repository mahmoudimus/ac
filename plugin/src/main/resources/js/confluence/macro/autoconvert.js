(function () {
    AJS.bind("init.rte", function () {
        var data = WRM.data.claim("com.atlassian.plugins.atlassian-connect-plugin:confluence-atlassian-connect-autoconvert-resources.connect-autoconvert-data");
        var arrayLength = data.length;

        console.log("found [ "+arrayLength+" ] autoconvert definitions");

        if (arrayLength>0) {
            for (var i = 0; i < arrayLength; i++) {
                var macroName = data[i].macroName;
                // TODO convert pattern to regex
                var pattern = data[i].autoconvert.pattern;

                console.log("registering autoconvert handler for [ " + macroName + " ] and pattern [ " + pattern + " ]");

                tinymce.plugins.Autoconvert.autoConvert.addHandler(function (uri, node, done) {
                    var matches = uri.source.match(pattern);
                    if (matches) {
                        console.log("matched autoconvert pattern [ " + pattern + " ] with uri [ " + uri + " ]");
                        var params = {};
                        // TODO generate macro parameters from matched groups
//                    for (var j = 0; j < autoConvertMappings[i].parameters.length; j++) {
//                        params[autoConvertMappings[i].parameters[j]] = matches[j + 1];
//                    }
                        var macro = {
                            name: macroName,
                            params: params
                        };
                        tinymce.plugins.Autoconvert.convertMacroToDom(macro, done, done);
                    } else {
                        console.log("did not match autoconvert pattern [ " + pattern + " ] with uri [ " + uri + " ]");
                        done();
                    }

                });
            }

        }

    });
})();
