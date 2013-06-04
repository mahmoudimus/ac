AP.require(["env", "request", "bigpipe", "dialog"], function (env, request, bigpipe, dialog) {

  // check that modules were returned and have some of the expected values set on them
  $("#amd-env").text((!!env && !!env.getUser).toString());
  $("#amd-request").text((!!request && !!request.__target__).toString());
  $("#amd-bigpipe").text((!!bigpipe && !!bigpipe.subscribe).toString());
  $("#amd-dialog").text((!!dialog && !!dialog.getButton).toString());

});
