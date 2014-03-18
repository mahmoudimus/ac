_AP.define("dialog/button", ["_dollar"], function($) {

    function button(options){
        this.$el = $('<button />')
            .text(options.text)
            .addClass('aui-button aui-button-' + options.type)
            .addClass(options.additionalClasses);

        this.isEnabled = function(){
            return !(this.$el.attr('aria-disabled') === "true");
        };

        this.setEnabled = function(enabled){
            //cannot disable a noDisable button
            if(options.noDisable === true && this.isEnabled() === false){
                return false;
            }
            this.$el.attr('aria-disabled', !enabled);
        };
        this.setEnabled(true);

        this.click = function(listener){
            if (listener) {
                this.$el.bind("ra.dialog.click", listener);
            } else {
                this.dispatch(true);
            }
        };

        this.dispatch = function (result) {
            var name = result ? "done" : "fail";
            options.actions && options.actions[name] && options.actions[name]();
        };

        this.setText = function(text){
            if(text){
                this.$el.text(text);
            }
        };

    }

    return {
        submit: function(actions){
            return new button({
                type: 'primary',
                text: 'submit',
                additionalClasses: 'ap-dialog-submit',
                actions: actions
            });
        },
        cancel: function(actions){
            return new button({
                type: 'link',
                text: 'cancel',
                noDisable: true,
                additionalClasses: 'ap-dialog-cancel',
                actions: actions
            });
        }
    };

});

