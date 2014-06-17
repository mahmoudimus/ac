(function(){
    define(["iframe/host/_util", "iframe/host/_dollar"], function() {
        _AP.require(["host/_util"], function(util){

            module("Host Utils", {
                setup: function() {
                    this.container = $("<div>container</div>").attr("id", "qunit-container").appendTo("body");
                },
                teardown: function() {
                    this.container.remove();
                }
            });
            test("escapeSelector enables selection of ids with funny characters", function(){
                var id = "com.blarhmee.hsometh-in4_5gan[ot43]458@he$rThing",
                text = "hello world";

                this.container.append('<div id="' + id + '">' + text + '</div>');
                equal($('#' + util.escapeSelector(id)).text(), text);
            });

            test("all reserved characters are escaped", function() {
                var reserved = "!\"#$%&'()*+,.\/:;<=>?@[\\]^`{|}~";
                equal(util.escapeSelector(reserved), "\\!\\\"\\#\\$\\%\\&\\'\\(\\)\\*\\+\\,\\.\\/\\:\\;\\<\\=\\>\\?\\@\\[\\\\\\]\\^\\`\\{\\|\\}\\~");
            });

        });

    });

})();
