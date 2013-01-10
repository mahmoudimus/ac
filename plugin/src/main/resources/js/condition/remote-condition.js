/*
  Hides UI elements that are protected by remote conditions.  These are all hacks hiding is supported
  by the module types directly
 */
(function ($, global) {
  var AP = global.AP = global.AP || {};
  function hide() {
    if (!AP.remoteConditionsHidden) {
      AP.remoteConditionsHidden = true;
      function hide($items) {
        $items.each(function (index, element) {
          var element$ = $(element);
          element$.addClass("hidden");
          if (element$.parent()[0].tagName == 'LI') {
            element$.parent().addClass("hidden");
          }
        });
      }

      // Connect any Remotable Plugin hosted Web Items to a dialog that loads the appropriate IFrame Servlet.
      hide($(".remote-condition"));

      // Look for jira issue tabs
      hide($("#issue-tabs a[id$='-remote-condition']"));

      // Look for jira project tabs
      hide($(".tabs a[id$='-remote-condition-panel']"));
    }
  }
  AJS.toInit(function () {
    hide();
  });
  AP.RemoteConditions = {
    hide: hide
  };
}(AJS.$, this));
