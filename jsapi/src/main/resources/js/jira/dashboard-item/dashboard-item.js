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
              var extensionContainer = $element.find(".ap-container");
              if(extensionContainer.length === 1){
                connectHost.broadcastEvent('jira_dashboard_item_edit', {
                  addon_key: extensionContainer.data('addon_key'),
                  key: extensionContainer.data('key')
                });
              }
            }
        };
    };
});

//                 init: function (state, xdm) {
//                     // register handle for the edit button in jira (if needed)
//                     $(xdm.iframe).on('dashboardItemEdit', function(){
//                         xdm.triggerDashboardItemEdit();
//                     });
//                 },
//                 internals: {
//                     setDashboardItemTitle: function(title) {
//                         // TODO this should be replaced with a valid gadget API
//                         var dashboardItemTitle = $($(this.iframe).parents('.gadget-container').find('h3.dashboard-item-title')[0]);
//                         dashboardItemTitle.text(title);
//                     },
//                     isDashboardItemEditable: function(callback) {
//                         var configureOption = $(this.iframe).parents('.gadget-container').find('li.configure');
//                         callback(configureOption.length != 0);
//                     }
//                 },
//                 stubs: ["triggerDashboardItemEdit"]
//             };
//         });
//     });

//     define('atlassian-connect/connect-dashboard-item', function() {
//         return function(){
//             return {
//                 render: function(){
//                 },
//                 renderEdit: function($element){
//                     $element.find('iframe').trigger('dashboardItemEdit');
//                 }
//             };
//         };
//     });


//     $(document).ready(function() {

//         $("body").on("resized",".ap-container", function(e, dimensions) {
//             var resizedParents = $(e.target).parents(".gadget-inline");
//             if (resizedParents.length > 0) {
//                 var inlineGadgetContainer = resizedParents.first();
//                 var gadgetId = inlineGadgetContainer.attr("id");
//                 var gadget = new AG.InlineGadgetAPI(inlineGadgetContainer);
//                 var layout = AG.DashboardManager.getLayout();
//                 var result = layout.getGadgets().filter(function (g) {
//                     return "gadget-".concat(g.getId()) == gadgetId;
//                 });
//                 result[0].resize();
//                 layout.refresh();
//             }
//         });

//     });


// })(AJS.$, define);