require.config({
  map: {
    '*': {
      '_xdm': '_xdmMock'
    }
  }
});

var xdmMock = {
  init: function() {}
};

define('_xdmMock', function () {
  return function() {
    return xdmMock;
  };
});

define(["confluence", "_rpc"], function(confluence, _rpc) {

  xdmMock.saveMacro = sinon.spy();
  xdmMock.closeMacroEditor = sinon.spy();
  _rpc.init();

  module("Confluence Plugin", {
    setup: function() {
      xdmMock.saveMacro.reset();
      xdmMock.closeMacroEditor.reset();
    }
  });

  test("saveMacro", function() {
    confluence.saveMacro("1");
    ok(xdmMock.saveMacro.calledOnce);
  });

  test("closeMacro", function(){
    confluence.closeMacroEditor();
    ok(xdmMock.closeMacroEditor.calledOnce);
  });

});
