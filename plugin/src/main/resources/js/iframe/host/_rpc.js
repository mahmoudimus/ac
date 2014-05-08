_AP.define("_rpc", ["_dollar", "_xdm"], function ($, XdmRpc) {

  "use strict";

  var each = $.each,
      extend = $.extend,
      isFn = $.isFunction,
      rpcCollection = [],
      apis = {},
      stubs = [],
      internals = {},
      inits = [],
      isInited,
      xdmOptions = {};

  return {

    extend: function (config) {
      if (isFn(config)) config = config();
      extend(apis, config.apis);
      extend(internals, config.internals);
      stubs = stubs.concat(config.stubs || []);

      if (isFn(config.xdmOptions)) config.xdmOptions = config.xdmOptions();
      extend(xdmOptions, config.xdmOptions);
      var init = config.init;
      if (isFn(init)) inits.push(init);
      return config.apis;
    },

    // init connect host side
    // options = things that go to all init functions
    // xdmOptions = module key / addon key etc.
    init: function (options, xdmConfig) {
      options = options || {};

      extend(xdmConfig, xdmOptions);

        // add stubs for each public api
        each(apis, function (method) { stubs.push(method); });

        // empty config for add-on-side ctor
        var rpc = new XdmRpc($, xdmConfig, {remote: stubs, local: internals});
        rpcCollection[rpc.id] = rpc;
        each(inits, function (_, init) {
          // console.log("INITS", options);
          try { init(extend({}, options, rpc), rpc); }
          catch (ex) { console.log(ex); }
        });

    }

  };

});
