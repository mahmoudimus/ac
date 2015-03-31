(function($, require){

    require(["ac/confluence/macro/autoconvert"], function(Autoconvert) {
        AJS.bind("init.rte", Autoconvert.registerAutoconvertHandlers);
    });
})(AJS.$, require);