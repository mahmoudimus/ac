_AP.define("_rpc", ["_dollar", "_xdm"], function ($, XdmRpc) {

  "use strict";

  var each = $.each,
      extend = $.extend,
      isFn = $.isFunction,
      rpcCollection = [],
      apis = {},
      stubs = [],
      internals = {},
      inits = [];

  return {

    extend: function (config) {
      if (isFn(config)) config = config();
      extend(apis, config.apis);
      extend(internals, config.internals);
      stubs = stubs.concat(config.stubs || []);

      var init = config.init;
      if (isFn(init)) inits.push(init);
      return config.apis;
    },

    // init connect host side
    // options = things that go to all init functions

    init: function (options, xdmConfig) {
      options = options || {};

        // add stubs for each public api
        each(apis, function (method) { stubs.push(method); });

        // TODO: stop copying internals and fix references instead (fix for events going across add-ons when they shouldn't)
        var rpc = new XdmRpc($, xdmConfig, {remote: stubs, local: $.extend({}, internals)});
        rpcCollection[rpc.id] = rpc;
        each(inits, function (_, init) {
          try { init(extend({}, options), rpc); }
          catch (ex) { console.log(ex); }
        });

    }

  };

});
