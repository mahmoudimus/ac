var dialog = require('speakeasy/dialog');
var $ = require('speakeasy/jquery').jQuery;
var addMessage = require('speakeasy/messages').add;
var host = require('speakeasy/host');
var contextPath = window.contextPath === undefined ? host.findContextPath() : window.contextPath;

function sendRegistrationToken(url, token, callbacks) {
    $.ajax({
      url: contextPath + "/rest/remoteapps/latest/installer",
      type: 'POST',
      beforeSend: function(jqXHR, settings) {
        jqXHR.setRequestHeader("X-Atlassian-Token", "nocheck");
      },
      data: {
          url : url,
          token : token
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

function wordwrap( str, width, brk, cut ) {

    brk = brk || '\n';
    width = width || 75;
    cut = cut || false;

    if (!str) { return str; }

    var regex = '.{1,' +width+ '}';

    return str.match( RegExp(regex, 'g') ).join( brk );

}


$(document).ready(function() {
    $('#rp-install').click(function(e) {
        e.preventDefault();
        dialog.openOnePanelDialog({
                    id : 'remoteapps-install-dialog',
                    width : 700,
                    height : 500,
                    header : 'Install Remote App',
                    content : require('./install-dialog').render({}),
                    submit : function(dialog, callbacks) {
                        var url = $('#remoteapps-url').val();
                        var token = $('#remoteapps-token').val();
                        sendRegistrationToken(url, token, callbacks);
                    },
                    submitClass : 'remoteapps-submit'
                });
        $.ajax({
          url: contextPath + "/plugins/servlet/oauth/consumer-info",
          type: 'GET',
          dataType : 'xml',
          success: function(data) {
            var publicKey = "-----BEGIN PUBLIC KEY-----\n" +
                            wordwrap($(data).find('publicKey').text(), 64) +
                            "\n-----END PUBLIC KEY-----";
            
            $('#oauth-consumer-key').text($(data).find('key').text());
            $('#oauth-consumer-public-key').text(publicKey);
          }
        });
    });
});