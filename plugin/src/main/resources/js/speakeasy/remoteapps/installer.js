var dialog = require('speakeasy/dialog');
var $ = require('speakeasy/jquery').jQuery;
var addMessage = require('speakeasy/messages').add;
var host = require('speakeasy/host');
var contextPath = window.contextPath === undefined ? host.findContextPath() : window.contextPath;

function sendRegistrationToken(url, callbacks) {
    $.ajax({
      url: contextPath + "/rest/remoteapps/latest/installer",
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
    $('#rp-install').click(function(e) {
        e.preventDefault();
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