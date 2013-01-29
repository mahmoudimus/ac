AJS.toInit(function ($) {

  var AP = _AP;

  function createEventHandler() {
    return function(event) {
      event.preventDefault();
      var $el = $(event.target);
      var href = $el.attr("href");
      var options = {header: $el.text()};
      var re = /[?&](width|height)=([^&]+)/g, match;
      while (match = re.exec(href)) {
        options[match[1]] = decodeURIComponent(match[2]);
      }
      AP.makeDialog(href, options).show();
    };
  }
  // jquery 1.7 or later
  if ($().on) {
    // Connect any web items to the dialog.  Necessary to bind to dynamic action cogs in JIRA
    $(window.document).on("click", ".ap-dialog", createEventHandler());
  } else {
    // Bind to all static links
    var $dialogWebItems = $(".ap-dialog");
    $dialogWebItems.each(function (index, el) {
      var $el = $(el);
      $el.click(createEventHandler());
    });
  }

});
