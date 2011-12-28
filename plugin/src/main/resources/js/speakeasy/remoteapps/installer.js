var openOnePanelDialog = require('speakeasy/dialog').openOnePanelDialog;
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

function openKeygen() {

    var dialog = new AJS.Dialog({width:600, height:700, id:'keygen-dialog'});
    dialog.addHeader("Generated RSA Keys for OAuth");
    var keygenDialogContents = require('./keygen-dialog').render({});
    dialog.addPanel("Keygen", keygenDialogContents, "panel-body");
    dialog.show();
    $.ajax({
          url: contextPath + "/rest/remoteapps/latest/installer/keygen",
          type: 'POST',
          dataType : 'json',
          success: function(data) {
            $('#remoteapps-public-key').text(data.publicKey);
            $('#remoteapps-private-key').text(data.privateKey);
          }
        });
    $('#keygen-close-link').click(function(e) {
        dialog.remove();
    });
}


$(document).ready(function() {
    $('#rp-install').click(function(e) {
        e.preventDefault();
        var dialog = openOnePanelDialog({
                    id : 'remoteapps-install-dialog',
                    width : 700,
                    height : 550,
                    header : 'Install Remote App',
                    content : require('./install-dialog').render({
                      contextPath : contextPath
                    }),
                    submit : function(dialog, callbacks) {
                        var url = $('#remoteapps-url').val();
                        var token = $('#remoteapps-token').val();
                        sendRegistrationToken(url, token, callbacks);
                    },
                    submitClass : 'remoteapps-submit'
                });
        $('#rp-keygen').click(function(e) {
            e.preventDefault();
            dialog.remove();
            openKeygen();
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