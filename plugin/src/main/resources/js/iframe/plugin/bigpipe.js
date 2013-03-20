AP.define("bigpipe", ["_dollar", "env"], function ($, env) {

  "use strict";

  var each = $.each;

  function BigPipe() {

    var started,
        closed,
        channels = {},
        subscribers = {},
        buffers = {},
        counter = 0;

    function poll(url) {
      $.ajax({
        url: url + "/" + (counter += 1),
        headers: {"Accept": "application/json"},
        success: function (response) {
          deliver(response, url);
        },
        error: function (xhr, status, ex) {
          if (xhr.status !== 200) {
            // despite being a real error, treat any non-200 as a termination
            // signal to prevent an infinite request cycle
            deliver({items: [], pending: []}, url);
          }
          else {
            $.handleError(ex || (xhr && xhr.responseText) || status);
          }
        }
      });
    }

    function deliver(response, url) {
      if (response) {
        var items = response.items;
        var pending = response.pending;
        if (items.length > 0 || pending.length > 0) {
          each(items, function (i, item) {
            publish(item);
          });
          if (pending.length > 0) {
            each(channels, function (channelId, open) {
              if (open && $.inArray(channelId, pending) < 0) {
                close(channelId);
              }
            });
            poll(url);
          }
          else {
            close();
          }
        }
        else if (!closed) {
          close();
        }
      }
    }

    function publish(event) {
      var channelId = event.channelId;
      channels[channelId] = true;
      if (subscribers[channelId]) subscribers[channelId](event);
      else (buffers[channelId] = buffers[channelId] || []).push(event);
    }

    function close(channelId) {
      if (channelId) {
        publish({channelId: channelId, complete: true});
        delete subscribers[channelId];
        channels[channelId] = false;
      }
      else if (!closed) {
        each(subscribers, close);
        closed = true;
      }
    }

    var self = {

      start: function (options) {
        options = options || {};
        var baseUrl = options.localBaseUrl || env.meta("local-base-url");
        var requestId = options.requestId;
        if (!started && baseUrl && requestId) {
          var ready = options.ready || {items: [], pending: [0]};
          deliver(ready, baseUrl + "/bigpipe/request/" + requestId);
          started = true;
        }
        return self;
      },

      subscribe: function (channelId, subscriber) {
        if (subscribers[channelId]) {
          throw new Error("Channel '" + channelId + "' already has a subscriber");
        }
        if (subscriber) {
          subscribers[channelId] = subscriber;
          each(buffers[channelId], function (i, event) {
            publish(event);
          });
          delete buffers[channelId];
          if (closed) close(channelId);
        }
        return self;
      }

    };

    return self;
  }

  // construct the main bigpipe instance servicing the initial page load
  var main = BigPipe();

  // auto-subscribe it to the html channel
  main.subscribe("html", function (event) {
    if (!event.complete) {
      $("#" + event.contentId).removeClass("bp-loading").html(event.content);
      env.resize();
    }
  });

  // merge the ctor with the main bigpipe instance as an api convenience
  return $.extend(BigPipe, main);

});
