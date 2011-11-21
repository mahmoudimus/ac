var dialog = require('speakeasy/dialog');
var $ = require('speakeasy/jquery').jQuery;
var addMessage = require('speakeasy/messages').add;
var host = require('speakeasy/host');
var installDialog = require('speakeasy/user/install/install');

function sendRegistrationToken(url, callbacks) {
    $.ajax({
      url: host.findContextPath() + "/rest/remoteapps/latest/installer",
      type: 'POST',
      beforeSend: function(jqXHR, settings) {
        jqXHR.setRequestHeader("X-Atlassian-Token", "nocheck");
      },
      data: {
          url : url,
          token : ''
      },
      success: function(data) {
          addMessage('success', {body: "Registration successful"});
          callbacks.success();
      },
      error: function(xhr) {
          AJS.messages.error('#remoteapps-errors', {body:xhr.responseText});
          callbacks.failure();
      }
    });
}


$(document).ready(function() {
    installDialog.addInstallLink(
            'remoteapps-install-link',
            "Install Remote App", function(e) {
                dialog.openOnePanelDialog({
                    id : 'remoteapps-install-dialog',
                    width : 500,
                    height : 450,
                    header : 'Install Remote App',
                    content : require('./install-dialog').render({}),
                    submit : function(dialog, callbacks) {
                        var url = $('#remoteapps-url').val();
                        sendRegistrationToken(url, callbacks);
                    },
                    submitClass : 'remoteapps-submit'
                });
            });
});