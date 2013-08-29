/**
 * Manages layers.
 *
 * There is a single global layer manager, AJS.LayerManager.global.
 * Additional instances can be created however this is rare.
 *
 * Layers are added by the push($el) method. Layers are removed by the
 * popUntil($el) method.
 *
 * popUntil's contract is that it pops all layers above & including the given
 * layer. This is used to support popping multiple layers.
 * Say we were showing a dropdown inside an inline dialog inside a dialog - we
 * have a stack of dialog layer, inline dialog layer, then dropdown layer. Calling
 * popUntil(dialog.$el) would hide all layers above & including the dialog.
 */
(function($) {

    function topIndexWhere(layerArr, fn) {
        var i = layerArr.length;
        while (i--) {
            if (fn(layerArr[i])) {
                return i;
            }
        }
        return -1;
    }

    function layerIndex(layerArr, $el) {
        return topIndexWhere(layerArr, function($layer) {
            return $layer[0] === $el[0];
        });
    }

    function topBlanketedIndex(layerArr) {
        return topIndexWhere(layerArr, function($layer) {
            return _AP.AJS.layer($layer).isBlanketed();
        });
    }

    function nextZIndex(layerArr) {
        var nextZIndex;
        if (layerArr.length) {
            var $topEl = layerArr[layerArr.length - 1];
            var zIndex = parseInt($topEl.css("z-index"));
            nextZIndex = (isNaN(zIndex) ? 0 : zIndex) + 100;
        }
        else {
            nextZIndex = 0;
        }
        return Math.max(3000, nextZIndex);
    }

    function LayerManager() {
        this._stack = [];
    }

    /**
     * Pushes a layer onto the stack.
     * The same element cannot be opened as a layer multiple times - if the
     * given element is already an open layer, this method throws an exception.
     * @param {element} $el  Element to push
     */
    LayerManager.prototype.push = function($el) {

        if (layerIndex(this._stack, $el) >= 0) {
            throw new Error("The given element is already an active layer");
        }

        var layer = _AP.AJS.layer($el);

        var zIndex = nextZIndex(this._stack);
        layer._showLayer(zIndex);

        if (layer.isBlanketed()) {
            if (topBlanketedIndex(this._stack) >= 0) {
                AJS.undim(); // remove blanket at old level
            }
            AJS.dim(false, zIndex - 20);
        }

        this._stack.push($el);
    };
    /**
     * Removes all layers above & including the given element.
     * If the given element is not an active layer, this method is a no-op.
     * @param {element} $el layer to pop.
     * @return The last layer that was popped.
     */
    LayerManager.prototype.popUntil = function($el) {
        var index = layerIndex(this._stack, $el);
        if (index < 0) {
            // layer not found
            return null;
        }

        // Pop from the top until the given layer is removed
        var removed = this._stack.slice(index);
        this._stack = this._stack.slice(0, index);
        
        var removedBlanketIndex = topBlanketedIndex(removed); // >=0 if the removed layers had a blanket
        if (removedBlanketIndex >= 0) {
            AJS.undim();
            var newTopBlanketedIndex = topBlanketedIndex(this._stack);
            if (newTopBlanketedIndex >= 0) {
                AJS.dim(false, this._stack[newTopBlanketedIndex].css("z-index") - 20);
            }
        }

        var $layer;
        while (removed.length) {
            $layer = removed.pop();
            _AP.AJS.layer($layer)._hideLayer();
        }
        return $layer;
    };

    /**
     * Pops the top layer, if it exists.
     * @return The layer that was popped, if it exists, otherwise null.
     */
    LayerManager.prototype.popTop = function() {
        if (!this._stack.length) {
            return null;
        }
        var $topLayer = this._stack[this._stack.length - 1];
        if (_AP.AJS.layer($topLayer).isModal()) {
            return null;
        }
        return this.popUntil($topLayer);
    };

    /**
     * Pops all layers above and including the top blanketed layer. If layers exist but none are blanketed,
     * this method does nothing.
     * @return The blanketed layer that was popped, if it exists, otherwise null.
     */
    LayerManager.prototype.popUntilTopBlanketed = function() {
        var i = topBlanketedIndex(this._stack);
        if (i < 0) {
            return null;
        }
        var $topBlanketedLayer = this._stack[i];
        if (_AP.AJS.layer($topBlanketedLayer).isModal()) {
            return null;
        }
        return this.popUntil($topBlanketedLayer);
    };

    _AP.AJS.LayerManager = LayerManager;

}(AJS.$));
