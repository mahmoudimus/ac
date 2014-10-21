(function(){
    var context = require.config({
        context: Math.floor(Math.random() * 1000000),
        baseUrl: 'base/src/main/resources/js/iframe/plugin'
    });

    window.xdmMock = {
        init: function() {}
    };

    var initStub = sinon.stub();

    AP.define("_rpc", function(){
        return {
            extend: function(func){
                if(typeof func === "function"){
                    func = func(window.xdmMock);
                }
                return func.apis;
            }, init: initStub
        };
    });

    function loadInitScript(container, callback){
        var g = document.createElement('script');
        g.src = "base/src/main/resources/js/iframe/plugin/_init.js";
        if(callback){
            g.onload = callback;
        }
        container.appendChild(g);
        return g;
    }

    module("_init", {
        setup: function(){
            this.fixture = document.createElement("div");
            this.fixture.id = 'init-fixture';
            document.body.appendChild(this.fixture);

        },
        teardown: function(){
            initStub.reset();
            document.body.removeChild(this.fixture);
        }
    });

    asyncTest('init gets called when no options are supplied', function(){
        var script = document.createElement("script");
        script.setAttribute("src", "/atlassian-connect/all.js");
        var container = document.getElementById("init-fixture");
        container.appendChild(script);
        loadInitScript(container, function(){
            ok(initStub.calledOnce);
            start();
        });
    });

    asyncTest('options are passed from script tag', function(){
        var script = document.createElement("script");
        script.setAttribute("src", "/atlassian-connect/all.js");
        script.setAttribute("data-options", "resize:false");

        var container = document.getElementById("init-fixture");
        container.appendChild(script);
        loadInitScript(container, function(){
            deepEqual(initStub.getCall(0).args[0], {resize: false});
            start();
        });

    });

    asyncTest('options are passed from non script tag with the correct id', function(){
        var div = document.createElement("div");
        div.setAttribute("data-options", "sizeToParent:false");
        div.setAttribute("id", "ac-iframe-options");
        var container = document.getElementById("init-fixture");
        container.appendChild(div);
        loadInitScript(container, function(){
            deepEqual(initStub.getCall(0).args[0], {sizeToParent: false});
            start();
        });

    });

    asyncTest('no options are passed', function(){
        var script = document.createElement("script");
        script.setAttribute("src", "/atlassian-connect/all.js");
        var container = document.getElementById("init-fixture");
        container.appendChild(script);
        loadInitScript(container, function(){
            deepEqual(initStub.getCall(0).args[0], {});
            start();
        });

    });

    asyncTest('both script and div are present. script takes precendence', function(){
        var script = document.createElement("script");
        script.setAttribute("src", "/atlassian-connect/all.js");
        script.setAttribute("data-options", "resize:false");
        var container = document.getElementById("init-fixture");
        container.appendChild(script);

        var div = document.createElement("div");
        div.setAttribute("data-options", "sizeToParent:false");
        div.setAttribute("id", "ac-iframe-options");
        container.appendChild(div);

        loadInitScript(container, function(){
            deepEqual(initStub.getCall(0).args[0], {resize: false});
            start();
        });

    });


})();
