/**
 * Entry point for xdm messages on the host product side.
 */
_AP.define("host/main", ["_dollar", "_xdm", "host/_addons", "host/_status_helper", "messages/main", "_ui-params"], function ($, XdmRpc, addons, statusHelper, messages, uiParams) {

  var xhrProperties = ["status", "statusText", "responseText"],
      xhrHeaders = ["Content-Type"],
      events = (AJS.EventQueue = AJS.EventQueue || []),
      defer = window.requestAnimationFrame || function (f) {setTimeout(f,10); },
      log = (window.AJS && window.AJS.log) || (window.console && window.console.log) || (function() {});

  function contentDiv(ns) {
    return $("#embedded-" + ns);
  }

  /**
  * @name Options
  * @class
  * @property {String}  ns            module key
  * @property {String}  src           url of the iframe
  * @property {String}  w             width of the iframe
  * @property {String}  h             height of the iframe
  * @property {String}  dlg           is a dialog (disables the resizer)
  * @property {String}  simpleDlg     deprecated, looks to be set when a confluence macro editor is being rendered as a dialog
  * @property {Boolean} general       is a page that can be resized
  * @property {String}  productCtx    context to pass back to the server (project id, space id, etc)
  * @property {String}  key           addon key from the descriptor
  * @property {String}  uid           id of the current user
  * @property {String}  ukey          user key
  * @property {String}  data.timeZone timezone of the current user
  * @property {String}  cp            context path
  */

  /**
  * @param {Options} options These values come from the velocity template and can be overridden using uiParams
  */
  function create(options) {

    $.extend(options, uiParams.fromUrl(options.src));

    var ns = options.ns,
        homeId = "ap-" + ns,
        $home = $("#" + homeId),
        $content = contentDiv(ns),
        contentId = $content.attr("id"),
        channelId = "channel-" + ns,
        initWidth = options.w || "100%",
        initHeight = options.h || "0",
        start = new Date().getTime(),
        isDialog = !!options.dlg,
        isInlineDialog = ($content.closest('.aui-inline-dialog').length > 0),
        isSimpleDialog = !!options.simpleDlg,
        isGeneral = !!options.general,
        // json string representing product context
        productContextJson = options.productCtx,
        isInited;

    function publish(name, props) {
      props = $.extend(props || {}, {moduleKey: ns});
      events.push({name: name, properties: props});
    }

    var timeout = setTimeout(function () {
      timeout = null;
      statusHelper.showloadTimeoutStatus($home);
      var $timeout = $home.find(".ap-load-timeout");
      $timeout.find("a.ap-btn-cancel").click(function () {
        statusHelper.showLoadErrorStatus($home);
        $nexus.trigger(isDialog ? "ra.dialog.close" : "ra.iframe.destroy");
      });
      layoutIfNeeded();
      publish("plugin.iframetimedout", {elapsed: new Date().getTime() - start});
    }, 20000);

    function preventTimeout() {
      if (timeout) {
        clearTimeout(timeout);
        timeout = null;
      }
    }

    function getDialogButtons() {
      return $nexus.data("ra.dialog.buttons");
    }

    function getDialogButton(name) {
      return $nexus.data("ra.dialog.buttons").getButton(name);
    }

    var rpc = new XdmRpc($, {
      remote: options.src,
      remoteKey: options.key,
      container: contentId,
      channel: channelId,
      props: {width: initWidth, height: initHeight}
    }, {
      remote: [
        "dialogMessage",
        // !!! JIRA specific !!!
        "setWorkflowConfigurationMessage"
      ],
      local: {
        init: function () {
          if (!isInited) {
            isInited = true;
            preventTimeout();
            $content.addClass("iframe-init");
            var elapsed = new Date().getTime() - start;
            statusHelper.showLoadedStatus($home);
            layoutIfNeeded();
            $nexus.trigger("ra.iframe.init");
            publish("plugin.iframeinited", {elapsed: elapsed});
          }
        },
        resize: debounce(function (width, height) {
          // debounce resizes to avoid excessive page reflow
          if (!isDialog) {
            // dialog content plugins do not honor resize requests, since their content size is fixed
            $("iframe", $content).css({width: width, height: height});
            if(isInlineDialog){
                _AP.require(["inline-dialog"], function (inlineDialog) {
                    inlineDialog.resizeInlineDialog($content, width, height);
                });
            }
          }
        }),
        sizeToParent: debounce(function() {
          // sizeToParent is only available for general-pages
          if (isGeneral) {
            // This adds border between the iframe and the page footer as the connect addon has scrolling content and can't do this
            $iframe.addClass("full-size-general-page");
            function resizeHandler() {
              var height = $(document).height() - AJS.$("#header > nav").outerHeight() - AJS.$("#footer").outerHeight() - 20;
              $("iframe", $content).css({width: "100%", height: height + "px"});
            }
            $(window).on('resize', resizeHandler);
            resizeHandler();
          }
          else {
            // This is only here to support integration testing
            // see com.atlassian.plugin.connect.test.pageobjects.RemotePage#isNotFullSize()
            $iframe.addClass("full-size-general-page-fail");
          }
        }),
        getLocation: function () {
          return window.location.href;
        },
        getUser: function () {
          // JIRA 5.0, Confluence 4.3(?)
          var meta = AJS.Meta,
              fullName = meta ? meta.get("remote-user-fullname") : null;
          if (!fullName) {
            // JIRA 4.4, Confluence 4.1, Refapp 2.15.0
            fullName = $("a#header-details-user-fullname, .user.ajs-menu-title, a#user").text();
          }
          if (!fullName) {
            // JIRA 6, Confluence 5
            fullName = $("a#user-menu-link").attr("title");
          }
          return {fullName: fullName, id: options.uid, key: options.ukey};
        },
        getTimeZone: function () {
          return options.data.timeZone;
        },
        showMessage: function (name, title, body, options) {
          return messages.showMessage(name, title, body, options);
        },
        clearMessage: function (id) {
          return messages.clearMessage(id);
        },
        setDialogButtonEnabled: function (name, enabled) {
          var button = getDialogButton(name);
          if (button) button.setEnabled(enabled);
        },
        isDialogButtonEnabled: function (name, callback) {
          var button = getDialogButton(name);
          callback(button ? button.isEnabled() : void 0);
        },
        createDialog: function(dialogOptions) {
          _AP.require("dialog", function(dialog) {
            dialog.create(options.key, productContextJson, dialogOptions);
          });
        },
        closeDialog: function() {
          _AP.require("dialog", function(dialog) {
            // TODO: only allow closing from same plugin key?
            dialog.close();
          });
        },
        hideInlineDialog: function() {
            _AP.require(["inline-dialog"], function (inlineDialog) {
                inlineDialog.hideInlineDialog($content, $nexus);
            });
        },

        request: function (args, success, error) {
          // add the context path to the request url
          var url = options.cp + args.url;
          // reduce the xhr object to the just bits we can/want to expose over the bridge
          function toJSON(xhr) {
            var json = {headers: {}};
            // only copy key properties and headers for transport across the bridge
            $.each(xhrProperties, function (i, v) { json[v] = xhr[v]; });
            // only copy key response headers for transport across the bridge
            $.each(xhrHeaders, function (i, v) { json.headers[v] = xhr.getResponseHeader(v); });
            return json;
          }
          function done(data, textStatus, xhr) {
            success([data, textStatus, toJSON(xhr)]);
          }
          function fail(xhr, textStatus, errorThrown) {
            error([toJSON(xhr), textStatus, errorThrown]);
          }
          var headers = {};
          $.each(args.headers || {}, function (k, v) { headers[k.toLowerCase()] = v; });
          // Disable system ajax settings. This stops confluence mobile from injecting callbacks and then throwing exceptions.
          $.ajaxSettings = {};

          // execute the request with our restricted set of inputs
          $.ajax({
            url: url,
            type: args.type || "GET",
            data: args.data,
            dataType: "text", // prevent jquery from parsing the response body
            contentType: args.contentType,
            headers: {
              // */* will undo the effect on the accept header of having set dataType to "text"
              "Accept": headers.accept || "*/*",
              // send the client key header to force scope checks
              "AP-Client-Key": options.key
            }
          }).then(done, fail);
        },
        // !!! JIRA specific !!!
        getWorkflowConfiguration: function (uuid, callback) {
          if(!/^[\w|-]+$/.test(uuid)){
            throw new Error("Invalid workflow ID");
          }
          var value,
          selector = $("#postFunction\\.config-"+uuid)[0];

          // if the matching selector has an id that starts with the correct string
          if(selector && selector.id.match(/postFunction\.config\-/).length === 1){
            value = $(selector).val();
          } else {
            throw ("Workflow configuration not found");
          }
          callback(value);
        },
        // !!! Confluence specific !!!
        saveMacro: function(updatedParams) {
          _AP.require("confluence/macro/editor", function(editor) {
            editor.saveMacro(updatedParams);
          });
        },
        closeMacroEditor: function () {
          _AP.require("confluence/macro/editor", function (editor) {
            editor.close();
          });
        },
        getMacroData: function(callback){
          _AP.require("confluence/macro/editor", function (editor) {
            editor.getMacroData(callback);
          });
        }
      }
    });

    statusHelper.showLoadingStatus($home);

    var $nexus = $content.parents(".ap-servlet-placeholder"),
        $iframe = $("iframe", $content);

    $iframe.data("ap-rpc", rpc);

    function layoutIfNeeded() {
      var $stats = $(".ap-stats", $home);
      $stats.removeClass("hidden");
      if (isSimpleDialog) {
        var panelHeight = $nexus.parent().height();
        $iframe.parents(".ap-servlet-placeholder, .ap-container").height(panelHeight);
        var containerHeight = $iframe.parents(".ap-container").height(),
            iframeHeight = containerHeight;
        if ($stats.find(".ap-status:visible").length > 0) {
            iframeHeight -= $stats.outerHeight(true);
        }
        $iframe.height(iframeHeight);
        $content.height(iframeHeight);
      }
    }

    layoutIfNeeded();

    // a simplified version of underscore's debounce
    function debounce(fn, wait) {
      var timeout;
      return function() {
        var ctx = this,
            args = [].slice.call(arguments);
        function later() {
          timeout = null;
          fn.apply(ctx, args);
        }
        if (timeout) {
          clearTimeout(timeout);
        }
        timeout = setTimeout(later, wait || 50);
      };
    }

    // wireup dialog buttons if appropriate
    var dialogButtons = getDialogButtons();
    if (dialogButtons) {
      dialogButtons.each(function (name, button) {
        button.click(function (e, callback) {
          if (isInited) {
            rpc.dialogMessage(name, callback);
          }
          else {
            callback(true);
          }
        });
      });
    }

    // !!! JIRA specific !!!
    var done = false;
    $(document).delegate("#add_submit, #update_submit", "click", function (e) {
      if (!done) {
        e.preventDefault();
        rpc.setWorkflowConfigurationMessage(function (either) {
          if (either.valid) {
            $("#postFunction\\.config-" + either.uuid).val(either.value);
            done = true;
            $(e.target).click();
          }
        });
      }
    });
    // !!! end JIRA !!!

    // register the rpc bridge with the addons module
    addons.get(options.key).add(rpc);

    // clean up when the iframe is removed by other scripts coordinating through the $nexus
    $nexus.bind("ra.iframe.destroy", function () {
      addons.get(options.key).remove(rpc);
      rpc.destroy();
    });

    $nexus.trigger("ra.iframe.create");
  }

  return function (options) {
    var attemptCounter = 0;
    function doCreate() {
        //If the element we are going to append the iframe to doesn't exist in the dom (yet). Wait for it to appear.
        if(contentDiv(options.ns).length === 0 && attemptCounter < 10){
            setTimeout(function(){
                attemptCounter++;
                doCreate();
            }, 50);
            return;
        }
      // make sure the content div is empty
      contentDiv(options.ns).find("iframe").each(function (_, iframe) {
        var rpc = $(iframe).data("ap-rpc");
        if (rpc) rpc.destroy();
      });
      // create the new iframe
      create(options);
    }
    if(typeof ConfluenceMobile !== "undefined"){
      doCreate();
    } else if ($.isReady) {
      // if the dom is ready then this is being run during an ajax update;
      // in that case, defer creation until the next event loop tick to ensure
      // that updates to the desired container node's parents have completed
      defer(doCreate);
    }
    else {
      AJS.toInit(function(){
        // Load after confluence editor has finished loading content.
        if(AJS.Confluence && AJS.Confluence.EditorLoader && AJS.Confluence.EditorLoader.load){
         
          /*
          NOTE: for some reason, the confluence EditorLoader will 404 sometimes on create page.
          Because of this, we need to pass our create function as both the success and error callback so we always get called
           */
          AJS.Confluence.EditorLoader.load(doCreate,doCreate);
        } else {
          doCreate();
        }
      });
    }
  };

});

// Legacy global namespace
// TODO: should be able to express this as _AP.create = _AP.require("host/main"). Requires changes in _amd.js
if (!_AP.create) {
  _AP.require(["host/main"], function(main) {
    _AP.create = main;
  });
}

if(typeof ConfluenceMobile !== "undefined"){
  //confluence will not run scripts loaded in the body of mobile pages by default.
  ConfluenceMobile.contentEventAggregator.on("render:pre:after-content", function(a, b, content) {
    window['eval'].call(window, $(content.attributes.body).find(".ap-iframe-body-script").html());
  });
}