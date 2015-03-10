(function () {
    AJS.bind("init.rte", function () {
        var autoconvertDefs = WRM.data.claim("com.atlassian.plugins.atlassian-connect-plugin:confluence-atlassian-connect-autoconvert-resources.connect-autoconvert-data");
        var numAutoconvertDefs = autoconvertDefs.length;
        console.log("found [ " + numAutoconvertDefs + " ] autoconvert definitions");

        if (numAutoconvertDefs > 0) {
            for (var i = 0; i < numAutoconvertDefs; i++) {
                var handler = function (i) {
                    var macroName = autoconvertDefs[i].macroName;
                    var urlParameter = autoconvertDefs[i].autoconvert.urlParameter;
                    var pattern = autoconvertDefs[i].matcherBean.pattern;

                    // build a regex from the defined autoconvert pattern
                    pattern = escapePattern(pattern);
                    pattern = pattern.replace('{}', '.*?');
                    console.log("registering autoconvert handler for [ " + macroName + " ] and pattern [ " + pattern + " ]");

                    return function (uri, node, done) {
                        var matches = uri.source.match(pattern);

                        if (matches) {
                            console.log("matched autoconvert pattern [ " + pattern + " ] with uri [ " + uri + " ]");
                            var params = {};

                            if (urlParameter != null) {
                                params[urlParameter] = "" + matches;
                            }

                            var macro = {
                                name: macroName,
                                params: params
                            };
                            tinymce.plugins.Autoconvert.convertMacroToDom(macro, done, done);
                        } else {
                            console.log("did not match autoconvert pattern [ " + pattern + " ] with uri [ " + uri + " ]");
                            done();
                        }

                    }
                }(i);
                tinymce.plugins.Autoconvert.autoConvert.addHandler(handler);
            }
        }
    });
    // escape everything that could interfere with the regex, except curly braces
    function escapePattern(str) { return str.replace(/[\-\[\]\/\(\)\*\+\?\.\\\^\$\|]/g, "\\$&"); }
})();