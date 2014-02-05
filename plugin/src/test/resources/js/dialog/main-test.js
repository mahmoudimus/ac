
define(['dialog/main'], function() {

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
        this.layerSpy = {
          changeSize: sinon.spy()
        };

        AJS.dialog2 = sinon.stub().returns(this.dialogSpy);
        AJS.layer = sinon.stub().returns(this.layerSpy);

        this.server = sinon.fakeServer.create();
      },
      teardown: function() {
        this.server.restore();
        // remove any dialog elements
        $(".ap-aui-dialog2").remove();
        dialog.close();
        // clean up mock
        _AP.AJS = null;
      }
    });

    test("Dialog exists", function() {
      ok(dialog.create, "Dialog create function exists");
    });

    test("Dialog create launches an xhr", function() {
      this.server.respondWith("GET", /.*my-plugin\/blah/,
        [200, { "Content-Type": "text/html" }, 'This is the <span id="my-span">content</span>']);

      dialog.create("my-plugin", "{}", {
        id: "my-dialog",
        key: "blah"
      });
      equal(1, $("#my-dialog").length, "Dialog element was created");
      ok(this.dialogSpy.show.calledOnce, "Dialog was shown");

      this.server.respond();
      equal(1, $("#my-span").length, "Dialog content was rendered");
    });

    test("Dialog create takes a size argument", function() {
      this.server.respondWith("GET", /.*my-plugin\/blah/,
        [200, { "Content-Type": "text/html" }, 'This is the <span id="my-span">content</span>']);

      dialog.create("my-plugin", "{}", {
        id: "my-dialog",
        key: "blah",
        size: "xlarge"
      });
      ok($("#my-dialog").is(".ap-aui-dialog2-xlarge"), "Size argument was passed to dialog");
    });

    test("Dialog create takes a titleId argument", function() {
      this.server.respondWith("GET", /.*my-plugin\/blah/,
        [200, { "Content-Type": "text/html" }, 'This is the <span id="my-span">content</span>']);

      dialog.create("my-plugin", "{}", {
        id: "my-dialog",
        key: "blah",
        titleId: "my-title-id"
      });
      equal($("#my-dialog").attr("aria-labelledby"), "my-title-id", "TitleId attribute was passed to dialog");
    });

    test("Dialog create takes a size arguments", function() {
      this.server.respondWith("GET", /.*my-plugin\/blah/,
        [200, { "Content-Type": "text/html" }, 'This is the <span id="my-span">content</span>']);

      dialog.create("my-plugin", "{}", {
        id: "my-dialog",
        key: "blah",
        width: "111px",
        height: "222px"
      });
      equal("111px", this.layerSpy.changeSize.args[0][0], "Width was set on layer");
      equal("222px", this.layerSpy.changeSize.args[0][1], "Height was set on layer");
    });

    test("Dialog close", function() {
      this.server.respondWith("GET", /.*my-plugin\/blah/,
        [200, { "Content-Type": "text/html" }, 'This is the <span id="my-span">content</span>']);

      dialog.create("my-plugin", "{}", {
        id: "my-dialog",
        key: "blah"
      });
      dialog.close();
      ok(this.dialogSpy.hide.calledOnce, "Dialog close was called");
    });
  });
});
