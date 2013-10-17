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
      createXdm: function(fixture){
        var f = fixture || 'xdm-emit.html';
        return new XdmRpc({
          remoteKey: 'myremotekey',
          remote: this.getBaseUrl() + '/base/src/test/resources/fixtures/' + f + '?oauth_consumer_key=jira:12345',
          container: 'qunit-container',
          channel: 'testchannel',
          props: {}
        }, {
          local: [],
          remote: {}
        });
      },
      getBaseUrl: function(){
        return window.location.origin;
      }
    });

    test('creates an iframe', function () {
      var xdm = this.createXdm();
      equal($("iframe#" + this.iframeId()).length, 1, "Iframe was created");
      ok(xdm.isActive(), 'XDM is active');
      ok(xdm.isHost, 'XDM is host');

    });

    test('destroys an iframe', function () {
      var xdm = this.createXdm();
      equal($("iframe#" + this.iframeId()).length, 1, "Iframe was created");
      xdm.destroy();
      //TODO: test post message handler is unbound.
      equal($("iframe#" + this.iframeId()).length, 0, "Iframe was destroyed");
    });

    test('messages are received', function () {
      var xdm = this.createXdm();
      xdm.events.on('clientevent', function (e){
        equal(e, '12345');
        start();
      });
      stop();
    });

    test('messages are sent', function () {
      var xdm = this.createXdm('xdm-emit-on.html');
      xdm.events.on('clientevent', function(e){
        equal(e, '9876');
        start();
      });
      stop();
      $("iframe#" + this.iframeId()).load(function(){
        xdm.events.emit('hostevent', '9876');
      });
      
    });

  });
});
