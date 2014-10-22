_AP.define("host/main", ["_dollar", "_rpc", "_ui-params", "host/_util"], function ($, rpc, uiParams, util) {

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
        contentId = $content.attr("id"),
        channelId = "channel-" + ns,
        initWidth = options.w || "100%",
        initHeight = options.h || "0",
        isDialog = !!options.dlg;

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
        xdm.productContext = options.productContext;
      }
    });

    rpc.init(options, xdmOptions);

  }

  return function (options) {
    create(options);
  };

});

function makeid()
{
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for( var i=0; i < 5; i++ )
        text += possible.charAt(Math.floor(Math.random() * possible.length));

    return text;
}

function insertIframeContent(iframe, clientCode){
    var script = iframe[0].contentWindow.document.createElement("script");
    script.type = "text/javascript";
    script.innerHTML = clientCode;
    iframe[0].contentWindow.document.body.appendChild(script);
}

function createConnectIframe(appendTo, clientCode){
    var addonDomain = window.location.origin,
    addonKey = 'addon-key' + makeid(),
    moduleKey = 'module-key' + makeid(),
    container = $('<div />').attr('id', 'embedded-' + addonKey + '__' + moduleKey).addClass('iframecontainer');
    appendTo.append(container);

    _AP.require(['host/main'], function(main){
        main({
            ns: addonKey + '__' + moduleKey,
            key: addonKey,
            cp: '',
            uid: 'someUserId',
            ukey: 'someuserkey',
            w: '',
            h: '',
            src: addonDomain + '/assets/js/blank.html?xdm_e=' + encodeURIComponent(addonDomain) + '&xdm_c=channel-' + addonKey + '__' + moduleKey + '&cp=&lic=none',
            productCtx: '{}',
            data: {},
            "timeZone":"Europe/London"
        });
        var iframe = appendTo.find('iframe');
        iframe.load(function(){
            insertIframeContent(iframe, clientCode);
        });
    });
}

function removeConnectIframes(){
    //remove old iframes before creating new ones.
    $(".runable iframe").trigger("ra.iframe.destroy");
    $(".iframecontainer").remove();
}

function makeButton(container){
    return $("<button />").addClass("aui-button example-button").text("run example").click(function(){
        removeConnectIframes();
        var code = $(container).find("textarea.demo").val();
        createConnectIframe($(container), code);
    });
}

$(function(){
    $(".runable").each(function(){
        $(this).append(makeButton(this));
    });
});

// a content resolver that does nothing except return blank
window._AP.contentResolver = {
  resolveByParameters: function(options){

    var promise = jQuery.Deferred(function(defer){
        var addonDomain = window.location.origin,
        url = addonDomain + '/assets/js/blank.html?xdm_e=' + encodeURIComponent(addonDomain) + '&xdm_c=channel-' + options.addonKey + '__' + options.moduleKey + '&cp=&lic=none',
        html = '<div id="embedded-' + options.addonKey + '__' + options.moduleKey + '" class="iframecontainer dialog-iframe-container">' + 
        "<script>" + 
        "_AP.require(['host/main'], function(main){" +
        "main({" + 
            "ns: '" + options.addonKey + '__' + options.moduleKey + "'," + 
            'key: "' + options.addonKey + '",' + 
            'cp: "",' + 
            "uid: 'someUserId'," + 
            "ukey: 'someuserkey'," + 
            "w: '100%'," + 
            "h: '100%'," + 
            "src: '" + url + "'," + 
            "productCtx: '{}'," + 
            "data: {}," +
            "uiParams: " + (JSON.stringify(options.uiParams) || "{}") +
        '});' + 
      '});' +
    "</script></div>";
    defer.resolve(html);
    }).promise();
    return promise;
  }
};
