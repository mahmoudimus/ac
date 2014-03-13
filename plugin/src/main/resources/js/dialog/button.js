_AP.define("dialog/dialog-button", ["_dollar"], function($) {

    function button(options){
        this.$el = $('<button />')
            .text(options.text)
            .addClass('aui-button aui-button-' + options.type);

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

        this.setEnabled(true);
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

