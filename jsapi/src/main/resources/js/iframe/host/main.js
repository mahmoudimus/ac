/**
* confluence and jira both break amd (by removing define.amd)
*/
_AP.addonAttemptCounter = {};

define('ac/create', ['ac/jira'], function(jira){
  connectHost.defineModule("jira", jira);
  connectHost.defineModule("cookie", connectHostCookie);
  connectHost.defineModule("history", connectHostHistory);
  connectHost.defineModule(connectHostRequest);

  connectHost.onIframeEstablished(function(data){
    data.$el.closest(".ap-content").addClass('iframe-init');
  });

  function convertConnectOptions(data){
    data.ns = data.key;
    data.key = data.key.replace(data.addon_key + "__",'');
    data.containerId = "embedded-" + data.ns;
    data.options = {
      isFullPage: (data.general === "1"),
      autoresize: (data.general === "1"),
      user: {
        timeZone: data.timeZone,
        fullName: data.fullName,
        uid: data.uid,
        ukey: data.ukey
      },
      history: {
        state: (window.location.hash ? window.location.hash.substr(2) : "")
      },
      productContext: JSON.parse(data.productCtx),
      contextPath: data.cp
    };
    return data;
  }

  /**
  * This function retries for compatibility with JIRA project templates
  * (and i'm sure other places too).
  **/
  return {
    appendConnectAddon: function(data){
      var aniFrame = (function(){
        return window.requestAnimationFrame  ||
              window.webkitRequestAnimationFrame ||
              window.mozRequestAnimationFrame ||
              function( callback ){
                window.setTimeout(callback, 50);
              };
      })();
      var convertedData = convertConnectOptions(data);
      var connectAddon = connectHost.create(convertedData);

      _AP.addonAttemptCounter[convertedData.containerId] = 0;
      function doAppend() {
        var container = document.getElementById(convertedData.containerId);
        _AP.addonAttemptCounter[convertedData.containerId]++;
        if(!container){
          // retry up to 10 times.
          if(_AP.addonAttemptCounter[convertedData.containerId] <= 10){
            aniFrame(doAppend);
          }
        } else {
          delete _AP.addonAttemptCounter[convertedData.containerId];
          connectAddon.appendTo(container);
        }
      }
      doAppend();
    }
  };
});
