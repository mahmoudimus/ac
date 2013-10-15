Qunit.require('dependencies/js/external/jquery/jquery.js');
Qunit.require('dependencies/js/atlassian/atlassian.js');
Qunit.require('js/iframe/host/_ap.js');
Qunit.require('js/iframe/_amd.js');
Qunit.require('js/iframe/host/_dollar.js');
Qunit.require('js/iframe/_events.js');
Qunit.require('js/iframe/_xdm.js');

_AP.require(["_xdm", "_dollar"], function(XdmRpc, $) {

  module('XDM host', {
    teardown: function() {
      $("iframe").remove();
    },
    iframeId: function() {
      return "easyXDM_qunit-fixture_provider";
    }
  });

  test('creates an iframe', function () {
    new XdmRpc({
      container: 'qunit-fixture'
    }, {});

    equal($("iframe").length, 1, "Iframe was created");
  });
});