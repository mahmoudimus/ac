var openOnePanelDialog = require('speakeasy/dialog').openOnePanelDialog;
var $ = require('speakeasy/jquery').jQuery;
var addMessage = require('speakeasy/messages').add;
var host = require('speakeasy/host');
var contextPath = window.contextPath === undefined ? host.findContextPath() : window.contextPath;

function sendRegistrationToken(url, callbacks) {
    $.ajax({
      url: contextPath + "/rest/remotable-plugins/latest/installer",
      type: 'POST',
      beforeSend: function(jqXHR, settings) {
        jqXHR.setRequestHeader("X-Atlassian-Token", "nocheck");
      },
      data: {
          url : url
      },
      success: function(data) {
          addMessage('success', {body: "Registration successful <a href='javascript:window.location.reload();'>(refresh)</a> "});
          callbacks.success();
      },
      error: function(xhr) {
          AJS.messages.error('#remotable-plugins-errors', {body:xhr.responseText});
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
          url: contextPath + "/rest/remotable-plugins/latest/installer/keygen",
          type: 'POST',
          dataType : 'json',
          success: function(data) {
            $('#remotable-plugins-public-key').text(data.publicKey);
            $('#remotable-plugins-private-key').text(data.privateKey);
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
                    id : 'remotable-plugins-install-dialog',
                    width : 700,
                    height : 550,
                    header : 'Install Remotable Plugin',
                    content : require('./install-dialog').render({
                      contextPath : contextPath
                    }),
                    submit : function(dialog, callbacks) {
                        var url = $('#remotable-plugins-url').val();
                        sendRegistrationToken(url, callbacks);
                    },
                    submitClass : 'remotable-plugins-submit'
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
    $.each($('tr[data-pluginkey]'), function() {
        var $row = $(this);
        var key = $row.attr("data-pluginkey");
        if ($('a.pk-viewsource', $row).length == 0) {
            $('.toolbar-item:last', $row).before('<li class="toolbar-item"><button class="toolbar-trigger rp-uninstall">Uninstall</button></li>')
        }

        $('.rp-uninstall', $row).click(function(e) {
            e.preventDefault();
            $.ajax({
              url: contextPath + "/rest/remotable-plugins/latest/uninstaller/" + key,
              type: 'DELETE',
              success: function(data) {
                addMessage('success', {body: "Uninstallation successful <a href='javascript:window.location.reload();'>(refresh)</a> "});
              },
              error: function(data) {
                  addMessage('error', {body: 'Unable to uninstall: ' + data.responseText});
              }
            });
        });
    });

    $('.rp-uninstall').click(function(e) {
        e.preventDefault();

    });
});