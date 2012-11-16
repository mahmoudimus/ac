AJS.toInit(function ($) {

  // Connect any Remotable Plugin hosted Web Items to a dialog that loads the appropriate IFrame Servlet.
  var $dialogWebItems = $(".ra-dialog");
  $dialogWebItems.each(function (index, el) {
    var $el = $(el);
    $el.click(function(event) {
      event.preventDefault();
      var href = $el.attr("href");
      var options = {header: $el.text()};
      var re = /[?&](width|height)=([^&]+)/g, match;
      while (match = re.exec(href)) {
        options[match[1]] = decodeURIComponent(match[2]);
      }
      RemotablePlugins.makeDialog(href, options).show();
    })
  });

});
