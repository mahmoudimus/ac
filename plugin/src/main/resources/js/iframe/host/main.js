/**
 * Entry point for xdm messages on the host product side.
 */
_AP.define("host/main", ["_dollar", "_xdm", "host/_addons", "_rpc", "_ui-params", "analytics/analytics", "host/_util"], function ($, XdmRpc, addons, rpc, uiParams, analytics, util) {

  var defer = window.requestAnimationFrame || function (f) {setTimeout(f,10); };

  function contentDiv(ns) {
    if(!ns){
      throw new Error("ns undefined");
    }
    return $("#embedded-" + util.escapeSelector(ns));
  }

  /**
  * @name Options
  * @class
  * @property {String}  ns            module key
  * @property {String}  src           url of the iframe
  * @property {String}  w             width of the iframe
  * @property {String}  h             height of the iframe
  * @property {String}  dlg           is a dialog (disables the resizer)
  * @property {String}  simpleDlg     deprecated, looks to be set when a confluence macro editor is being rendered as a dialog
  * @property {Boolean} general       is a page that can be resized
  * @property {String}  productCtx    context to pass back to the server (project id, space id, etc)
  * @property {String}  key           addon key from the descriptor
  * @property {String}  uid           id of the current user
  * @property {String}  ukey          user key
  * @property {String}  data.timeZone timezone of the current user
  * @property {String}  cp            context path
  */

  /**
  * @param {Options} options These values come from the velocity template and can be overridden using uiParams
  */
  function create(options) {

    $.extend(options, uiParams.fromUrl(options.src));

    var ns = options.ns,
        $content = contentDiv(ns),
        contentId = "embedded-" + ns,
        channelId = "channel-" + ns,
        initWidth = options.w || "100%",
        initHeight = options.h || "0",
        start = new Date().getTime(),
        isDialog = !!options.dlg,
        isInlineDialog = ($content.closest('.aui-inline-dialog').length > 0),
        isSimpleDialog = !!options.simpleDlg,
        isInited;

    if(typeof options.uiParams !== "object"){
      options.uiParams = {};
    }

    if(!!options.general) {
      options.uiParams.isGeneral = true;
    }

    if(options.dlg){
      options.uiParams.isDialog = true;
    }

    var xdmOptions = {
      remote: options.src,
      remoteKey: options.key,
      container: contentId,
      channel: channelId,
      props: {width: initWidth, height: initHeight},
      uiParams: options.uiParams
    };

    if(options.productCtx && !options.productContext){
      options.productContext = JSON.parse(options.productCtx);
    }

    rpc.extend({
      init: function(opts, xdm){
        xdm.analytics = analytics.get(xdm.addonKey, ns);
        xdm.analytics.iframePerformance.start();
        xdm.productContext = options.productContext;
      }
    });

    rpc.init(options, xdmOptions);

  }

  return function (options) {
    // AC-765 if we are about to replace an old instance of the connect iframe. Destroy it.
    $content = contentDiv(options.ns);
    $contentIframe = $content.find("iframe");
    if($contentIframe.length){
      $contentIframe.trigger('ra.iframe.destroy');
      $content.remove();
    }

    var attemptCounter = 0;
    function doCreate() {
        //If the element we are going to append the iframe to doesn't exist in the dom (yet). Wait for it to appear.
        if(contentDiv(options.ns).length === 0 && attemptCounter < 10){
            setTimeout(function(){
                attemptCounter++;
                doCreate();
            }, 50);
            return;
        }

      // create the new iframe
      create(options);
    }
    if(typeof ConfluenceMobile !== "undefined"){
      doCreate();
    } else if (AJS.$.isReady) {
      // if the dom is ready then this is being run during an ajax update;
      // in that case, defer creation until the next event loop tick to ensure
      // that updates to the desired container node's parents have completed
      defer(doCreate);
    }
    else {
      AJS.toInit(function hostInit(){
        // Load after confluence editor has finished loading content.
        if(AJS.Confluence && AJS.Confluence.EditorLoader && AJS.Confluence.EditorLoader.load) {
          /*
          NOTE: for some reason, the confluence EditorLoader will 404 sometimes on create page.
          Because of this, we need to pass our create function as both the success and error callback so we always get called
           */
          try {
            AJS.Confluence.EditorLoader.load(doCreate,doCreate);
          } catch(e) {
            try {
              doCreate();
            } catch(error) {
              AJS.log(error);
            }
          }

        } else {
          try {
              doCreate();
          } catch(error) {
            AJS.log(error);
          }
        }
      });
    }
  };

});

// Legacy global namespace
// TODO: should be able to express this as _AP.create = _AP.require("host/main"). Requires changes in _amd.js
if (!_AP.create) {
  _AP.require(["host/main"], function(main) {
    _AP.create = main;
  });
}

if(typeof ConfluenceMobile !== "undefined"){
  //confluence will not run scripts loaded in the body of mobile pages by default.
  ConfluenceMobile.contentEventAggregator.on("render:pre:after-content", function(a, b, content) {
    window['eval'].call(window, $(content.attributes.body).find(".ap-iframe-body-script").html());
  });
}
