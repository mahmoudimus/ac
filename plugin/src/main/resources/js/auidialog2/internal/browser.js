_AP.AJS._internal || ( _AP.AJS._internal = {});
(function($) {
    _AP.AJS._internal.browser = {};

    var supportsCalc = null;

    _AP.AJS._internal.browser.supportsCalc = function() {
        if (supportsCalc === null) {
            var $d = $('<div style="height: 10px; height: -webkit-calc(20px + 0); height: calc(20px);"></div>');
            supportsCalc = (20 === $d.appendTo(document.documentElement).height());
            $d.remove();
        }
        return supportsCalc;
    };
}(AJS.$));