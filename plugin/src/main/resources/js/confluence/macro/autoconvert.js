(function () {
    AJS.bind("init.rte", function () {
        var data = WRM.data.claim("com.atlassian.plugins.atlassian-connect-plugin:confluence-atlassian-connect-autoconvert-resources.connect-autoconvert-data");
        var arrayLength = data.length;

        console.log("found [ " + arrayLength + " ] autoconvert definitions");

        if (arrayLength > 0) {
            for (var i = 0; i < arrayLength; i++) {
                var handler = function (i) {
                    var macroName = data[i].macroName;
                    var urlParameter = data[i].autoconvert.urlParameter;
                    var pattern = data[i].autoconvert.pattern;

                    // build the regex from the pattern
                    pattern = escapePattern(pattern);
                    pattern = pattern.replace('{}', '.*?');
                    console.log("pattern is: "+ pattern);
                    console.log("registering autoconvert handler for [ " + macroName + " ] and pattern [ " + pattern + " ]");

                    return function (uri, node, done) {
                        var matches = uri.source.match(pattern);
                        console.log("matches: "+matches);

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