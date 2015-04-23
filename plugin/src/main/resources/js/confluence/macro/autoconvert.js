(function ($, define) {

    define("ac/confluence/macro/autoconvert", [], function () {

        /*
         This script adds autoconvert handlers to the confluence editor.

         It executes on editor initialisation, registering the handlers defined in the original add-on descriptors of whatever
         add-ons are installed. Each one is associated with a particular macro (although a macro can be associated with many
         handlers.

         On pattern match, the each handler will be asked if they match and will be processed by the first matching handler.
         The macro object is then created, using the matched uri and the information already existing in the JS context, and
         the macro is inserted into the editor.
         */

        var escapePattern = function (str) {
            return str.replace(/[\-\[\]\/\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
        };

        var replaceAll = function (find, replace, str) {
            return str.replace(new RegExp(find, 'g'), replace);
        };

        var isValidAutoconvertDef = function (autoconvertDef) {
            var pattern = autoconvertDef.matcherBean.pattern;
            var patternBlackList = [
                "http://",
                "https://",
                "http://{}",
                "https://{}",
                "http://{}.{}",
                "https://{}.{}",
                "http://{}.{}.{}",
                "https://{}.{}.{}"
            ]

            // check the url pattern is not banned
            for (i=0; i<patternBlackList.length; i++) {
                if (pattern == patternBlackList[i]) { return false; }
            }

            return autoconvertDef &&
                autoconvertDef.macroName &&
                autoconvertDef.autoconvert &&
                autoconvertDef.autoconvert.urlParameter &&
                autoconvertDef.matcherBean &&
                autoconvertDef.matcherBean.pattern;
        };

        var factory = function (autoconvertDef, callback) {

            return function (uri, node, done) {
                var macroName = autoconvertDef.macroName;
                var urlParameter = autoconvertDef.autoconvert.urlParameter;
                var pattern = autoconvertDef.matcherBean.pattern;

                var matches = uri.source.match(pattern);

                if (matches) {
                    var params = {};
                    if (urlParameter) {
                        params[urlParameter] = uri.source;
                    }
                    var macro = {name: macroName, params: params};
                    callback(macro, done);
                } else {
                    done();
                }
            }
        };

        return {
            escapePattern: escapePattern,
            replaceAll: replaceAll,
            factory: factory,
            registerAutoconvertHandlers: function (autoconvertDefs, tinymce) {
                if (autoconvertDefs) {
                    var numAutoconvertDefs = autoconvertDefs.length;
                    if (numAutoconvertDefs > 0) {
                        for (var i = 0; i < numAutoconvertDefs; i++) {
                            if (isValidAutoconvertDef(autoconvertDefs[i])) {
                                var pattern = autoconvertDefs[i].matcherBean.pattern;

                                console.log("before pattern is: "+ pattern);

                                // Consolidate any double up wildcards
                                while (pattern.indexOf('{}{}') != -1) {
                                    pattern = pattern.replace('{}{}', '{}')
                                }

                                // build a regex from the defined autoconvert pattern
                                pattern = escapePattern(pattern);
                                pattern = replaceAll('{}', '[^/]*?', pattern);
                                pattern = "^" + pattern + "$";

                                autoconvertDefs[i].matcherBean.pattern = pattern;

                                console.log("after pattern is: "+ pattern);

                                tinymce.plugins.Autoconvert.autoConvert.addHandler(factory(autoconvertDefs[i], function (macro, done) {
                                    tinymce.plugins.Autoconvert.convertMacroToDom(macro, done, function (jqXHR, textStatus, errorThrown) {
                                        console.log("error converting macro [ " + macro.name + " ] to dom elements [ " + errorThrown + " ]");
                                        done()
                                    });
                                }));
                            } else {
                                console.log("invalid autoconvert definition [ " + JSON.stringify(autoconvertDefs[i]) + " ]");
                            }
                        }
                    }
                }
            }
        };
    });
})(AJS.$, define);

