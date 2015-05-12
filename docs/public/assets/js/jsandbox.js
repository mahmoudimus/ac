(function(context){

  function makeid()
  {
      var text = "";
      var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

      for( var i=0; i < 5; i++ )
          text += possible.charAt(Math.floor(Math.random() * possible.length));

      return text;
  }

  function insertIframeContent(iframe, clientCode) {
      var script = iframe.contentWindow.document.createElement("script");
      script.type = "text/javascript";
      script.innerHTML = clientCode;
      iframe.contentWindow.document.body.appendChild(script);
  }

  function createConnectIframe(appendTo, clientCode){
      var baseUrl = window.location.origin + contextPath(),
      addonKey = 'addon-key' + makeid(),
      moduleKey = 'module-key' + makeid(),
      container = $('<div />').attr('id', 'embedded-' + addonKey + '__' + moduleKey).addClass('iframecontainer');
      appendTo.append(container);

      appendTo.on('ra.iframe.create', 'iframe', function () {
          $(this).load(function () {
              insertIframeContent(this,clientCode);
          });
      });

      _AP.create({
          ns: addonKey + '__' + moduleKey,
          key: addonKey,
          cp: '',
          uid: 'someUserId',
          ukey: 'someuserkey',
          w: '',
          h: '',
          src: baseUrl + '/assets/js/blank.html?xdm_e=' + encodeURIComponent(window.location.origin) + '&xdm_c=channel-' + addonKey + '__' + moduleKey + '&cp=&lic=none',
          productCtx: '{}',
          data: {},
          "timeZone":"Europe/London"
      });
  }

  function removeConnectIframes(){
      //remove old iframes before creating new ones.
      $(".runnable iframe").trigger("ra.iframe.destroy");
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
      $(".runnable").each(function(){
          $(this).append(makeButton(this));
      });
  });

  // a content resolver that does nothing except return blank
  context._AP.contentResolver = {
    resolveByParameters: function(options){
      var promise = jQuery.Deferred(function(defer){
          var baseUrl = window.location.origin + contextPath();
          url = baseUrl + '/assets/js/blank.html?xdm_e=' + encodeURIComponent(window.location.origin) + '&xdm_c=channel-' + options.addonKey + '__' + options.moduleKey + '&cp=&lic=none',
          html = '<div id="embedded-' + options.addonKey + '__' + options.moduleKey + '" class="iframecontainer dialog-iframe-container">' +
          "<script>" +
          "_AP.create(" +
          "{" +
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
        "</script></div>";
      defer.resolve(html);
      }).promise();
      return promise;
    }
  };


}(window));
