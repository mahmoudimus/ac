_AP.define("_rpc", ["_dollar", "_xdm"], function ($, XdmRpc) {

  "use strict";

  var each = $.each,
      extend = $.extend,
      isFn = $.isFunction,
      proxy = {},
      rpc,
      apis = {},
      stubs = [],
      internals = {},
      inits = [],
      isInited,
      xdmOptions = {};

  return {

    extend: function (config) {
      if (isFn(config)) config = config(proxy);
      extend(apis, config.apis);
      extend(internals, config.internals);
      stubs = stubs.concat(config.stubs || []);

      if (isFn(config.xdmOptions)) config.xdmOptions = config.xdmOptions(proxy);
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

      if (!isInited) {
        // add stubs for each public api
        each(apis, function (method) { stubs.push(method); });
        // empty config for add-on-side ctor
        rpc = this.rpc = new XdmRpc($, xdmConfig, {remote: stubs, local: internals});
        //rpc.init();
        extend(proxy, rpc);
        each(inits, function (_, init) {
          try { init(extend({}, options)); }
          catch (ex) { console.log(ex); }
        });
        isInited = true;
      }
    }

  };

});
