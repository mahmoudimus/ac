/**
* confluence.web.resources:almond deliberately breaks AMD modules so they are exposed globally.
* Turn it back into a defined module so others can use it as AMD if they wish.
*/
//    define('connect-host', function(){
//    return _AP;
//});
connectHost.onIframeEstablished(function(data){
    $("#embedded-" + data.extension.addon_key + "__" + data.extension.key).addClass('iframe-init');
});