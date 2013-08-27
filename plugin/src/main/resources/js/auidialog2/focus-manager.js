_AP.AJS.FocusManager = (function($) {
    function FocusManager() {
    }
    FocusManager.defaultFocusSelector = ":input:visible:enabled";
    FocusManager.prototype.enter = function($el) {
        var customFocusSelector = $el.attr('data-aui-focus-selector');
        var focusSelector = customFocusSelector || FocusManager.defaultFocusSelector;
        var $focusEl = $el.is(focusSelector) ? $el : $el.find(focusSelector);
        $focusEl.first().focus();
    };
    FocusManager.prototype.exit = function($el) {
        // AUI-1059: remove focus from the active element when dialog is hidden
        var activeElement = document.activeElement;
        if ($el[0] === activeElement || $el.has(activeElement).length) {
            activeElement.blur();
        }
    };

    FocusManager.global = new FocusManager();

    return FocusManager;
}(AJS.$));