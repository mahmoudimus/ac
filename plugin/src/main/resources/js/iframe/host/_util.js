_AP.define("host/_util", function () {
    return {
        escapeSelector: function( s ){
            return s.replace(/[!"#$%&'()*+,.\/:;<=>?@[\\\]^`{|}~]/g, "\\$&");
        }
    };
});