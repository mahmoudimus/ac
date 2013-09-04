(function($) {

    /**
     * Singleton layerManager instance
     * @type {LayerManager}
     */
    _AP.AJS.LayerManager.global = new _AP.AJS.LayerManager();

    $(document).on('keydown', function(e) {
        if (e.keyCode === 27) { // ESC
            var $popped = _AP.AJS.LayerManager.global.popTop();
            if ($popped) {
                e.preventDefault();
            }
        }
    }).on('click', '.aui-blanket', function(e) {
        var $popped = _AP.AJS.LayerManager.global.popUntilTopBlanketed();
        if ($popped) {
            e.preventDefault();
        }
    });


}(AJS.$));