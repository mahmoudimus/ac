_AP.define("dialog/dialog-button", ["_dollar"], function($) {

    function button(options){
        this.$el = $('<button />')
            .text(options.text)
            .addClass('aui-button aui-button-' + options.type);
        _callbacks = [];

        this.isEnabled = function(){
            return this.$el.attr('aria-disabled');
        };

        this.setEnabled = function(enabled){
            //cannot disable a noDisable button
            if(options.noDisable === true && this.isEnabled() === false){
                return false;
            }
            this.$el.attr('aria-disabled', !enabled);
        };

        this.click = function(listener){
            _callbacks.push(listener);
        };

        function trigger(){
            var args = arguments;
            $.each(_callbacks, function(i, val){
                val.apply(null, args);
            });
            $nexus.trigger('ra.dialog.close');
        }

        this.setEnabled(true);
        this.$el.click(trigger);
    }

    return {
        submit: function(){
            return new button({
                type: 'primary',
                text: 'submit'
            });
        },
        cancel: function(){
            return new button({
                type: 'link',
                text: 'cancel',
                noDisable: true
            });
        }
    };

});

