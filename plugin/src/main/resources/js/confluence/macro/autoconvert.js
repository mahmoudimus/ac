(function($, define){

    define("ac/confluence/macro/autoconvert", [], function() {

        /*
         This script adds autoconvert handlers to the confluence editor.

         It executes on editor initialisation, registering the handlers defined in the original add-on descriptors of whatever
         add-ons are installed. Each one is associated with a particular macro (although a macro can be associated with many
         handlers.

         On pattern match, the each handler will be asked if they match and will be processed by the first matching handler.
         The macro object is then created, using the matched uri and the information already existing in the JS context, and
         the macro is inserted into the editor.
         */

        var escapePattern = function(str) {
            return str.replace(/[\-\[\]\/\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
        };

        var replaceAll = function(find, replace, str) {
            return str.replace(new RegExp(find, 'g'), replace);
        };

        var factory = function (autoconvertDef, callback) {
                var macroName = autoconvertDef.macroName;
                var urlParameter = autoconvertDef.autoconvert.urlParameter;
                var pattern = autoconvertDef.matcherBean.pattern;

                // build a regex from the defined autoconvert pattern
                pattern = escapePattern(pattern);
                pattern = replaceAll('{}', '.*?', pattern);
                pattern = "^" + pattern + "$";

                return function (uri, node, done) {
                    var matches = uri.source.match(pattern);

                    if (matches) {
                        var params = {};

                        if (urlParameter != null) {
                            params[urlParameter] = uri.source;
                        }

                        var macro = {
                            name: macroName,
                            params: params
                        };
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
            registerAutoconvertHandlers: function (autoconvertDefs,tinymce) {
                if (autoconvertDefs) {
                    var numAutoconvertDefs = autoconvertDefs.length;
                    if (numAutoconvertDefs > 0) {
                        for (var i = 0; i < numAutoconvertDefs; i++) {
                            tinymce.plugins.Autoconvert.autoConvert.addHandler(factory(autoconvertDefs[i], function(macro, done) {
                                tinymce.plugins.Autoconvert.convertMacroToDom(macro, done, function(jqXHR, textStatus, errorThrown) {
                                    console.log("error converting macro [ "+macro.name+" ] to dom elements [ "+errorThrown+" ]");
                                    done()
                                });
                            }));
                        }
                    }
                }
            }
        };
    });
})(AJS.$, define);

