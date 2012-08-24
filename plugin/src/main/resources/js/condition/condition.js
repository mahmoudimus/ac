AJS.toInit(function ($) {

  var remoteConditionWebItems$ = $(".remote-condition");
  remoteConditionWebItems$.each(function (index, element) {
    var element$ = $(element);

    element$.click(function(event) {
      event.preventDefault();

      var dialog = RemoteApps.makeDialog(element$.attr("href"), {
        header: element$.text()
      });
      dialog.show();
    })
  });

})(AJS.$);
