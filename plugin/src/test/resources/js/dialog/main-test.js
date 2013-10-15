Qunit.require('dependencies/js/external/jquery/jquery.js');
Qunit.require('dependencies/js/atlassian/atlassian.js');
Qunit.require('js/iframe/host/_ap.js');
Qunit.require('js/iframe/_amd.js');
Qunit.require('js/iframe/host/_dollar.js');
Qunit.require('js/dialog/main.js');

_AP.require(["dialog"], function(dialog) {

  module("Dialog Main", {
    setup: function() {
      AJS.contextPath = function() { return ""; };
      this.dialogSpy = {
        show: sinon.spy(),
        on: sinon.spy(),
        remove: sinon.spy(),
        hide: sinon.spy()
      };
      _AP.AJS = {
        dialog2: sinon.stub().returns(this.dialogSpy)
      };
      this.server = sinon.fakeServer.create();
    },
    teardown: function() {
      this.server.restore();
      _AP.AJS = null;
      // remove any dialog elements
      $(".ap-aui-dialog2").remove();
    }
  });

  test("Dialog exists", function() {
    ok(dialog.create, "Dialog create function exists");
  });

  test("Dialog create launches an xhr", function() {
    this.server.respondWith("GET", /.*my-plugin\/blah/,
      [200, { "Content-Type": "text/html" }, 'This is the <span id="my-span">content</span>']);

    var context = JSON.stringify({});
    dialog.create("my-plugin", context, {
      id: "my-dialog",
      key: "blah"
    });
    equal(1, $("#my-dialog").length, "Dialog element was created");

    this.server.respond();
    equal(1, $("#my-span").length, "Dialog content was rendered");
  });
});
