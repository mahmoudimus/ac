//map AP.env.getUser to AP.user.getUser for compatibility.
if(AP._hostModules.user && AP.env) {
  AP._hostModules.env.getUser = AP._hostModules.user.getUser;  
  AP.env.getUser = AP._hostModules.user.getUser;
}

// support for deprecated sync history.getState() syntax.
if(AP._hostModules.history) {
  (function(){
    var state = "";
    if(AP._data.options.history && AP._data.options.history.state) {
      state = AP._data.options.history.state;
    }
    AP.history.popState(function(e){
      state = e.newURL;
    });

    var original_getState = AP._hostModules.history.getState.bind({});
    AP.history.getState = AP._hostModules.history.original_getState = function(cb){
      if(cb){
        original_getState(cb);
      } else {
        return state;
      }
    };
    var original_pushState = AP._hostModules.history.pushState.bind({});
    AP._hostModules.history.pushState = function(url){
        state = url;
        original_pushState(url);
    };

    var original_replaceState = AP._hostModules.history.replaceState.bind({});
    AP._hostModules.history.replaceState = function(newState){
        state = newState;
        original_replaceState(url);
    };

  }());
}