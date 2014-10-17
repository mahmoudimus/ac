var tests = [];
for (var file in window.__karma__.files) {
  if (window.__karma__.files.hasOwnProperty(file)) {
    if (/-test\.js$/.test(file)) {
      tests.push(file);
    }
  }
}

window.xdmMock; //setup a global to be filled with xdmMocks.
AP.define("_rpc", function(){
    return {
        extend: function(func){
            if(typeof func === "function"){
                func = func(window.xdmMock);
            }
            return func.apis;
        }
    };
});


requirejs.config({
    // Karma serves files from '/base'
    baseUrl: 'base/src/main/resources/js/iframe/plugin',

    paths: {
    },
    // ask Require.js to load these files (all our tests)
    deps: tests,

    // start test run, once Require.js is done
    callback: function(x){ 
        setTimeout(function(){
             window.__karma__.start(x);
        }, 1000);
    }
});

//tests will timeout after 5 seconds
window.QUnit.config.testTimeout = 5000;
