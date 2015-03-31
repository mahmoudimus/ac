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

        return {
            escapePattern: function(str) {
                return str.replace(/[\-\[\]\/\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
            },
            replaceAll: function(find, replace, str) {
                return str.replace(new RegExp(find, 'g'), replace);
            },
            factory: function (autoconvertDef) {
                var macroName = autoconvertDef.macroName;
                var urlParameter = autoconvertDef.autoconvert.urlParameter;
                var pattern = autoconvertDef.matcherBean.pattern;

                // build a regex from the defined autoconvert pattern
                pattern = this.escapePattern(pattern);
                pattern = this.replaceAll('{}', '.*?', pattern);
                pattern = "^" + pattern + "$";

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
            },
            registerAutoconvertHandlers: function () {
                var autoconvertDefs = WRM.data.claim("com.atlassian.plugins.atlassian-connect-plugin:confluence-atlassian-connect-autoconvert-resources.connect-autoconvert-data");
                if (autoconvertDefs) {
                    var numAutoconvertDefs = autoconvertDefs.length;
                    console.log("found [ " + numAutoconvertDefs + " ] autoconvert definitions");
                    if (numAutoconvertDefs > 0) {
                        for (var i = 0; i < numAutoconvertDefs; i++) {
                            tinymce.plugins.Autoconvert.autoConvert.addHandler(this.factory(autoconvertDefs[i]));
                        }
                    }
                }
            }
        };
    });
})(AJS.$, define);

