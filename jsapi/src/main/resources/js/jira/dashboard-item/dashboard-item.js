define("ac/jira/dashboard-item", [], function(){
  "use strict";

  function resizeGadget(target, height){
      var resizedParents = AJS.$(target).parents(".gadget-inline");
      if (resizedParents.length > 0) {
        var inlineGadgetContainer = resizedParents.first();
        var gadgetId = inlineGadgetContainer.attr("id");
        var gadget = new AG.InlineGadgetAPI(inlineGadgetContainer);
        var layout = AG.DashboardManager.getLayout();
        var result = layout.getGadgets().filter(function (g) {
          return "gadget-".concat(g.getId()) == gadgetId;
        });

        if(height){
          inlineGadgetContainer.find("iframe").css('height', height);
        }
        
        result[0].resize();
        layout.refresh();
      }
  }

  connectHost.onIframeEstablished(function(data){
    resizeGadget(data.$el);
  });

  AJS.$(document).ready(function() {
    AJS.$("body").on("resized",".ap-container", function(e, dimensions) {
      resizeGadget(e.target, dimensions.height);
    });
  });

  return {
    setDashboardItemTitle: function(title, callback){
      var container = document.getElementById(callback._context.extension_id);
      var dashboardItemTitle = $(container).parents('.gadget-container').find('h3.dashboard-item-title');
      dashboardItemTitle.text(title);
    },
    isDashboardItemEditable: function(callback){
      var container = document.getElementById(callback._context.extension_id);
      var configureOption = $(container).parents('.gadget-container').find('li.configure');
      callback(configureOption.length !== 0);
    }
  };
});

define('atlassian-connect/connect-dashboard-item', function() {
    return function(){
        return {
            render: function(){
            },
            renderEdit: function($element){
              var iframe = $element.find('iframe');
              var extensionContainer = iframe.closest('.ap-container');
              if(extensionContainer.length === 1){
                connectHost.broadcastEvent('jira_dashboard_item_edit', {
                  addon_key: extensionContainer.data('addonKey'),
                  key: extensionContainer.data('key')
                });
              }
            }
        };
    };
});