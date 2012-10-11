AJS.toInit(function ($) {

    // Connect any Remotable Plugin hosted Web Items to a dialog that loads the appropriate IFrame Servlet.
    var dialogWebItems$ = $(".ra-dialog");
    dialogWebItems$.each(function (index, element) {
        var element$ = $(element);
        element$.click(function(event) {
            event.preventDefault();
            var dialog = RemotablePlugins.makeDialog(element$.attr("href"), {
                header: element$.text()
            });
            dialog.show();
        })
    });

})(AJS.$);
