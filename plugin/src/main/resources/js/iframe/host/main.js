/**
 * Entry point for xdm messages on the host product side.
 */
_AP.define("host/main", ["_dollar", "_xdm", "host/_addons", "_rpc", "_ui-params"], function ($, XdmRpc, addons, rpc, uiParams) {

  var xhrProperties = ["status", "statusText", "responseText"],
      xhrHeaders = ["Content-Type"],
      events = (AJS.EventQueue = AJS.EventQueue || []),
      defer = window.requestAnimationFrame || function (f) {setTimeout(f,10); },
      log = (window.AJS && window.AJS.log) || (window.console && window.console.log) || (function() {});

  function contentDiv(ns) {
    return $("#embedded-" + ns);
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
        homeId = "ap-" + ns,
        $home = $("#" + homeId),
        $content = contentDiv(ns),
        contentId = $content.attr("id"),
        channelId = "channel-" + ns,
        initWidth = options.w || "100%",
        initHeight = options.h || "0",
        start = new Date().getTime(),
        isDialog = !!options.dlg,
        isInlineDialog = ($content.closest('.aui-inline-dialog').length > 0),
        isSimpleDialog = !!options.simpleDlg,
        isGeneral = !!options.general,
        // json string representing product context
        productContextJson = options.productCtx,
        isInited;


    var xdmOptions = {
      remote: options.src,
      remoteKey: options.key,
      container: contentId,
      channel: channelId,
      props: {width: initWidth, height: initHeight},
      uiParams: options.uiParams
    };
    rpc.extend({
      init: function(){
        console.log('i am init');
      }
    });

    rpc.init(options, xdmOptions);


    function publish(name, props) {
      props = $.extend(props || {}, {moduleKey: ns});
      events.push({name: name, properties: props});
    }

    var timeout = setTimeout(function () {
      timeout = null;
      statusHelper.showloadTimeoutStatus($home);
      var $timeout = $home.find(".ap-load-timeout");
      $timeout.find("a.ap-btn-cancel").click(function () {
        statusHelper.showLoadErrorStatus($home);
        $nexus.trigger(isDialog ? "ra.dialog.close" : "ra.iframe.destroy");
      });
      layoutIfNeeded();
      publish("plugin.iframetimedout", {elapsed: new Date().getTime() - start});
    }, 20000);

    function preventTimeout() {
      if (timeout) {
        clearTimeout(timeout);
        timeout = null;
      }
    }

    function getDialogButtons() {
      return $nexus.data("ra.dialog.buttons");
    }

    function getDialogButton(name) {
      return $nexus.data("ra.dialog.buttons").getButton(name);
    }


    function getProductContext(){
      return JSON.parse(productContextJson);
    }



    function prefixCookie(name){
      return options.key + '-' + options.ns + '-' + name;
    }

    // Do not delay showing the loading indicator if this is a dialog.
    var noDelay = (isDialog || isSimpleDialog || isInlineDialog);

    var $nexus = $content.parents(".ap-servlet-placeholder"),
        $iframe = $("iframe", $content);

    $iframe.data("ap-rpc", rpc);

    function layoutIfNeeded() {
      var $stats = $(".ap-stats", $home);
      $stats.removeClass("hidden");
      if (isSimpleDialog) {
        var panelHeight = $nexus.parent().height();
        $iframe.parents(".ap-servlet-placeholder, .ap-container").height(panelHeight);
        var containerHeight = $iframe.parents(".ap-container").height(),
            iframeHeight = containerHeight;
        if ($stats.find(".ap-status:visible").length > 0) {
            iframeHeight -= $stats.outerHeight(true);
        }
        $iframe.height(iframeHeight);
        $content.height(iframeHeight);
      }
    }

    layoutIfNeeded();

    // a simplified version of underscore's debounce
    function debounce(fn, wait) {
      var timeout;
      return function() {
        var ctx = this,
            args = [].slice.call(arguments);
        function later() {
          timeout = null;
          fn.apply(ctx, args);
        }
        if (timeout) {
          clearTimeout(timeout);
        }
        timeout = setTimeout(later, wait || 50);
      };
    }

    // wireup dialog buttons if appropriate
    var dialogButtons = getDialogButtons();
    if (dialogButtons) {
      dialogButtons.each(function (name, button) {
        button.click(function (e, callback) {
          if (isInited) {
            rpc.dialogMessage(name, callback);
          }
          else {
            callback(true);
          }
        });
      });
    }

    // clean up when the iframe is removed by other scripts coordinating through the $nexus
    $nexus.bind("ra.iframe.destroy", function () {
      addons.get(options.key).remove(rpc);
      rpc.destroy();
    });

    $nexus.trigger("ra.iframe.create");
  }

  return function (options) {
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
      // make sure the content div is empty
      contentDiv(options.ns).find("iframe").each(function (_, iframe) {
        var rpc = $(iframe).data("ap-rpc");
        if (rpc) rpc.destroy();
      });
      // create the new iframe
      create(options);
    }
    if(typeof ConfluenceMobile !== "undefined"){
      doCreate();
    } else if ($.isReady) {
      // if the dom is ready then this is being run during an ajax update;
      // in that case, defer creation until the next event loop tick to ensure
      // that updates to the desired container node's parents have completed
      defer(doCreate);
    }
    else {
      AJS.toInit(function(){
        // Load after confluence editor has finished loading content.
        if(AJS.Confluence && AJS.Confluence.EditorLoader && AJS.Confluence.EditorLoader.load){
         
          /*
          NOTE: for some reason, the confluence EditorLoader will 404 sometimes on create page.
          Because of this, we need to pass our create function as both the success and error callback so we always get called
           */
          AJS.Confluence.EditorLoader.load(doCreate,doCreate);
        } else {
          doCreate();
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
