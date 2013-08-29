(function($) {
    _AP.AJS._internal = _AP.AJS._internal || {};

    /**
     * @param {string} name The name of the widget to use in any messaging.
     * @param {function(new:{ $el: jQuery }, ?jQuery, ?Object)} Ctor
     *     A constructor which will only ever be called with "new". It must take a JQuery object as the first
     *     parameter, or generate one if not provided. The second parameter will be a configuration object.
     *     The returned object must have an $el property and a setOptions function.
     */
    _AP.AJS._internal.widget = function(name, Ctor) {
        var dataAttr = '_aui-widget-' + name;
        return function(selectorOrOptions, maybeOptions) {
            var selector;
            var options;
            if ($.isPlainObject(selectorOrOptions)) {
                options = selectorOrOptions;
            } else {
                selector = selectorOrOptions;
                options = maybeOptions;
            }

            var $el = selector && $(selector);
            
            var widget;
            if (!$el || !$el.data(dataAttr)) {
                widget = new Ctor($el, options || {});
                $el = widget.$el;
                $el.data(dataAttr, widget);
            } else {
                widget = $el.data(dataAttr);
                // options are discarded if $el has already been constructed
            }

            return widget;
        };
    };
}(AJS.$));