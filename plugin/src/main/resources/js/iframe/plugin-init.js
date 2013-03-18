// @todo make product-specific inclusions (e.g. jira) dynamic
AP.require(["_dollar", "env", "bigpipe", "jira"], function ($, env, bigpipe, jira) {

  "use strict";

  // deprecated, backward-compatibility extension of AP with pre-AMD plugins
  $.extend(AP, env, jira, {
    Meta: {get: env.meta},
    BigPipe: bigpipe
  });

  // initialization

  // find the script element that imported this code
  var options = {},
      $script = $("script[src*='/remotable-plugins/all']");
  if ($script && /\/remotable-plugins\/all(-debug)?\.js($|\?)/.test($script.attr("src"))) {
    // get its data-options attribute, if any
    var optStr = $script.attr("data-options");
    if (optStr) {
      // if found, parse the value into kv pairs following the format of a style element
      $.each(optStr.split(";"), function (i, nvpair) {
        nvpair = $.trim(nvpair);
        if (nvpair) {
          var nv = nvpair.split(":"), k = trim(nv[0]), v = trim(nv[1]);
          if (k && v != null) options[k] = v === "true" || v === "false" ? v === "true" : v;
        }
      });
    }
  }

  AP.init(options);

});
