(function () {

    function escapePattern(str) { return str.replace(/[\-\[\]\/\(\)\*\+\?\.\\\^\$\|]/g, "\\$&"); };
    function replaceAll(find, replace, str) { return str.replace(new RegExp(find, 'g'), replace); };

    var factory = function (autoconvertDef) {
        var macroName = autoconvertDef.macroName;
        var urlParameter = autoconvertDef.autoconvert.urlParameter;
        var pattern = autoconvertDef.matcherBean.pattern;

        // build a regex from the defined autoconvert pattern
        pattern = escapePattern(pattern);
        pattern = replaceAll('{}', '.*?', pattern);
        pattern = "^"+pattern+"$";

        console.log("registering autoconvert handler for [ " + macroName + " ] and pattern [ " + pattern + " ]");

        return function (uri, node, done) {
            var matches = uri.source.match(pattern);

            if (matches) {
                console.log("matched autoconvert pattern [ " + pattern + " ] with uri [ " + uri + " ]");
                var params = {};

                if (urlParameter != null) {
                    params[urlParameter] = uri.source;
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
    };

    AJS.bind("init.rte", function () {
        var autoconvertDefs = WRM.data.claim("com.atlassian.plugins.atlassian-connect-plugin:confluence-atlassian-connect-autoconvert-resources.connect-autoconvert-data");
        if(autoconvertDefs) {
            var numAutoconvertDefs = autoconvertDefs.length;
            console.log("found [ " + numAutoconvertDefs + " ] autoconvert definitions");
            if (numAutoconvertDefs > 0) {
                for (var i = 0; i < numAutoconvertDefs; i++) {
                    tinymce.plugins.Autoconvert.autoConvert.addHandler(factory(autoconvertDefs[i]));
                }
            }
        }
    });

})();