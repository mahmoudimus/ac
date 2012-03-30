AJS.toInit(function($) {

    function disableButtons(ids) {
        var btns = [];
        $.each(ids, function() {
            btns.push($('.' + this));
        });
        $.each(btns, function() {
            this.attr('disabled', 'disabled');
        });
        return {
            'enable' : function() {
                $.each(btns, function() {
                    this.removeAttr('disabled');
                })
            }
        }
    }

    function openOnePanelDialog(webItem$, options) {
        var defDialogOptions = {
            width:$(window).width() * .50,
            height:$(window).height() * .50,
            header:webItem$.text(),
            content:"",
            submit:function (dialog, callback) {
                callback.success();
            },
            cancel:function (dialog, callback) {
                callback.success();
            },
            submitLabel:'Submit',
            submitClass:'ra-dialog-submit',
            cancelClass:'ra-dialog-cancel'
        };

        var dialogOptions = $.extend({}, defDialogOptions, options);
        var dialog = new AJS.Dialog(dialogOptions);
        dialog.addHeader(dialogOptions.header);
        dialog.addPanel("Main", dialogOptions.content, "ra-panel-body");
        dialog.addButton(dialogOptions.submitLabel, function (dialog) {
            var btns = disableButtons([dialogOptions.submitClass, dialogOptions.cancelClass]);
            dialogOptions.submit(dialog, {
                success:function () {
                    RemoteAppsRpc.onSubmit(function (result) {
                        if (result) {
                            dialog.remove();
                        }
                        else {
                            btns.enable();
                        }
                    });
                },
                failure:function () {
                    btns.enable();
                }
            });
        }, dialogOptions.submitClass);

        dialog.addButton("Cancel", function (dialog) {
            var btns = disableButtons([dialogOptions.submitClass, dialogOptions.cancelClass]);
            dialogOptions.cancel(dialog, {
                success:function () {
                    dialog.remove();
                },
                failure:function () {
                    btns.enable();
                }
            });
        }, dialogOptions.cancelClass);
        dialog.show();
        return dialog;
    }

    // Connect any Remote App hosted Web Items to a dialog that loads the appropriate IFrame Servlet.
    var dialogWebItems$ = $(".ra-dialog");
    dialogWebItems$.each(function(index, element) {
        var element$ = $(element);
        element$.click(function(event) {
            event.preventDefault();

            openOnePanelDialog(element$, { content: "<div class='ra-servlet-placeholder'>Loading...</div>"});
            var panelBody = $(".ra-panel-body");

            var loadingServlet = element$.attr("href");
            if (loadingServlet.indexOf("?") > 0)
            {
                loadingServlet += "&width=" + panelBody.width() + "&height=" + panelBody.height();
            }
            else
            {
                loadingServlet += "?width=" + panelBody.width() + "&height=" + panelBody.height();

            }

            $.ajax(loadingServlet, {
                accepts: 'text/html',
                success: function(data) {
                    $(".ra-servlet-placeholder").html(data);
                }
            })
        })
    });

})(AJS.$);