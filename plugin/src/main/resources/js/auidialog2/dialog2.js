(function($) {

    /*
     IE8 doesn't support calc(), so we emulate all our calc stuff here. This needs to be kept in sync with the CSS.
     */
    function makeCalcShimEvents() {
        var openDialogs = [];
        var $window = $(window);
        var windowHeight;
        var windowWidth;
        var setDialogHeight = function(dialog) {
            // fast hasClass evaluation, seeing as this runs in a resize loop
            var dialogClass = " " + dialog.$el[0].className + " ";
            function dialogHasClass(selector) {
                return dialogClass.indexOf(" " + selector + " ") >= 0;
            };

            var dialogSize = dialogHasClass('ap-aui-dialog2-small')   ? 'small'   :
                             dialogHasClass('ap-aui-dialog2-medium')  ? 'medium'  :
                             dialogHasClass('ap-aui-dialog2-large')   ? 'large'   :
                             dialogHasClass('ap-aui-dialog2-x-large') ? 'x-large' :
                                                                     'custom';
            var dialogFitsWidth;
            var dialogFitsHeight;
            switch(dialogSize) {
                case 'small':
                    dialogFitsWidth = windowWidth > 420;
                    dialogFitsHeight = windowHeight > 500;
                    break;
                case 'medium':
                    dialogFitsWidth = windowWidth > 620;
                    dialogFitsHeight = windowHeight > 500;
                    break;
                case 'large':
                    dialogFitsWidth = windowWidth > 820;
                    dialogFitsHeight = windowHeight > 700;
                    break;
                case 'x-large':
                    dialogFitsWidth = windowWidth > 1000;
                    dialogFitsHeight = windowHeight > 700;
                    break;
                default:
                    // custom sizers can do their own thing
                    dialogFitsWidth = true;
                    dialogFitsHeight = true;
            }
            dialog.$el
                .toggleClass('ap-aui-dialog2-fullscreen', !dialogFitsWidth)
                .css('height', windowHeight - 107 - (dialogFitsWidth ? 200 : 0));
            dialog.$el.find('.ap-aui-dialog2-content')
                .css('min-height', dialogFitsHeight ?   '' :
                                 windowHeight > 500 ? '193px' :
                                                      '93px');
        };
        var resizeHandler = function() {
            windowHeight = $window.height();
            windowWidth = $window.width();
            for(var i = 0, len = openDialogs.length; i < len; i++) {
                setDialogHeight(openDialogs[i]);
            }
        };

        var onShow = function (dialog) {
            if (!openDialogs.length) {
                windowHeight = $window.height();
                windowWidth = $window.width();
                $window.on('resize', resizeHandler);
            }
            setDialogHeight(dialog);
            openDialogs.push(dialog);
        };
        var onHide = function (dialog) {
            openDialogs = $.grep(openDialogs, function(openDialog) {
                return dialog !== openDialog;
            });
            if (!openDialogs.length) {
                $window.off('resize', resizeHandler);
            }
        };

        return {
            show: onShow,
            hide: onHide
        };
    }

    var calcShimEvents;

    function triggerCalcShimEvent(dialog, event) {
        if (!calcShimEvents) {
            calcShimEvents = _AP.AJS._internal.browser.supportsCalc() ? {} : makeCalcShimEvents();
        }
        calcShimEvents[event] && calcShimEvents[event](dialog);
    }

    function Dialog2($el) {
        this.$el = $el || $(aui.dialog.dialog2({}));
    }

    Dialog2.prototype.on = function(event, fn) {
        _AP.AJS.layer(this.$el).on(event, fn);
        return this;
    };
    
    Dialog2.prototype.show = function() {
        triggerCalcShimEvent(this, "show");
        _AP.AJS.layer(this.$el).show();
        return this;
    };

    Dialog2.prototype.hide = function() {
        triggerCalcShimEvent(this, "hide");
        _AP.AJS.layer(this.$el).hide();
        return this;
    };

    Dialog2.prototype.remove = function() {
        triggerCalcShimEvent(this, "hide");
        _AP.AJS.layer(this.$el).remove();
        return this;
    };

    _AP.AJS.dialog2 = _AP.AJS._internal.widget('dialog2', Dialog2);

    _AP.AJS.dialog2.on = function(eventName, fn) {
        _AP.AJS.layer.on(eventName, function(e, $el) {
            if ($el.is(".ap-aui-dialog2")) {
                fn.apply(this, arguments);
            }
        });
    };

    /* Live events */

    $(document).on('click', '.ap-aui-dialog2-header-close', function(e) {
        e.preventDefault();
        _AP.AJS.dialog2($(this).closest('.ap-aui-dialog2')).hide();
    });

})(AJS.$);