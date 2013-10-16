define(['iframe-host-xdm'], function() {

  _AP.require(["_xdm", "_dollar"], function(XdmRpc, $) {

    module('XDM host', {
      setup: function() {
        this.container = $("<div />").attr("id", "qunit-container").appendTo("body");
      },
      teardown: function() {
        this.container.remove();
      },
      iframeId: function() {
        return "easyXDM_qunit-container_provider";
      },
      createXdm: function(){
        return new XdmRpc({
          remoteKey: 'myremotekey',
          remote: this.getBaseUrl() + '/?oauth_consumer_key=jira:12345',
          container: 'qunit-container',
          props: {}
        }, {});
      },
      getBaseUrl: function(){
        return window.location.origin;
      },
      iframeContents: function(){
        var i,
          container = '',
          scripts = [
            '_amd',
            'plugin/_util',
            'plugin/_dollar',
            'plugin/_rpc',
            '_events',
            '_xdm'
          ];

        for (i in scripts){
          container += '<script src="' +  '/js/iframe/' + scripts[i] + '.js"></script>';
        }
        return container;
      }
    });

    test('creates an iframe', function () {
      var xdm = this.createXdm();
      equal($("iframe").length, 1, "Iframe was created");
      ok(xdm.isActive(), 'XDM is active');
      ok(xdm.isHost, 'XDM is host');

    });

    test('destroys an iframe', function() {
      var xdm = this.createXdm();
      equal($("iframe").length, 1, "Iframe was created");
      xdm.destroy();
      //TODO: test post message handler is unbound.
      equal($("iframe").length, 0, "Iframe was destroyed");
    });

//    test('messages are sent', function(){
//      var xdm = this.createXdm(),
//        iframedoc = $('iframe')[0].contentWindow.document;
//      iframedoc.body.innerHTML = this.iframeContents();
//      window.postMessage('something', this.getBaseUrl());
//      ok(true, 'remove me');
//    });


  });
});
