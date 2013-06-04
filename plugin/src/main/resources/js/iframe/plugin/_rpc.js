AP.define("_rpc", ["_dollar", "_xdm-rpc"], function ($, XdmRpc) {

  "use strict";

  var each = $.each,
      extend = $.extend,
      isFn = $.isFunction,
      proxy = {},
      rpc,
      apis = {},
      stubs = ["init"],
      internals = {},
      inits = [],
      isInited;

  return {

    extend: function (config) {
      if (isFn(config)) config = config(proxy);
      extend(apis, config.apis);
      extend(internals, config.internals);
      stubs = stubs.concat(config.stubs || []);
      var init = config.init;
      if (isFn(init)) inits.push(init);
      return config.apis;
    },

    // inits the remote plugin on iframe content load
    init: function (options) {
      options = options || {};
      if (!isInited) {
        var config = {};
        // add stubs for each public api
        each(apis, function (method) { stubs.push(method); });
        rpc = new XdmRpc(config, {remote: stubs, local: internals});
        rpc.init();
        extend(proxy, rpc);
        each(inits, function (_, init) {
          try { init(extend({}, options)); }
          catch (ex) { $.handleError(ex); }
        });
        isInited = true;
      }
    }

  };

});
