AP.require(["env", "bigpipe"], function (env, BigPipe) {

  // subscribe to data produced by the initial request on the main BigPipe instance
  BigPipe.subscribe("data", dataSubscriber);

  // create a new big pipe instance via xhr to being a secondary series
  $.ajax({
    url: env.localUrl("/bigPipeTest?xhr"),
    dataType: "json",
    success: function (config) {
      var bigpipe = new BigPipe();
      bigpipe.start(config);
      bigpipe.subscribe("data", dataSubscriber);
    },
    error: function (xhr, status, ex) {
      if (window.console) console.error(xhr, status, ex);
    }
  });

  function dataSubscriber(event) {
    if (event.content) {
      var json = JSON.parse(event.content);
      $("#" + json.id).text(json.data);
      AP.resize();
    }
  }

});
