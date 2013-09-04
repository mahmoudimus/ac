AP.define("env", ["_dollar", "_rpc"], function ($, rpc) {

  "use strict";

  var apis = rpc.extend(function (remote) {

    return {

      apis: {

        // get the location of the host page
        //
        // @param callback  function (location) {...}
        getLocation: function (callback) {
          remote.getLocation(callback);
        },

        // get a user object containing the user's id and full name
        //
        // @param callback  function (user) {...}
        getUser: function (callback) {
          remote.getUser(callback);
        },

        // get current timezone - if user is logged in then this will retrieve user's timezone
        // the default (application/server) timezone will be used for unauthorized user
        //
        // @param callback  function (user) {...}
        getTimeZone: function (callback) {
          remote.getTimeZone(callback);
        },

        // fire an analytics event
        //
        // @param id  the event id.  Will be prepended with the prefix "p3.iframe."
        // @param props the event properties
        fireEvent: function (id, props) {
          console.log("AP.fireEvent deprecated; will be removed in future version");
        },

        // shows a message with body and title by id in the host application
        //
        // @param id    the message id
        // @param title   the message title
        // @param body    the message body
        showMessage: function (id, title, body) {
          remote.showMessage(id, title, body);
        },

        // clears a message by id in the host application
        //
        // @param id    the message id
        clearMessage: function (id) {
          remote.clearMessage(id);
        },

        // resize this iframe
        //
        // @param width   the desired width
        // @param height  the desired height
        resize: $.debounce(function (width, height) {
          var dim = apis.size(width, height);
          remote.resize(dim.w, dim.h);
        }, 50)

      }

    };

  });

  return $.extend(apis, {

    meta: function (name) {
      return $("meta[name='ap-" + name + "']").attr("content");
    },

    localUrl: function (path) {
      return this.meta("local-base-url") + (path == null ? "" : path);
    },

    size: function (width, height) {
      var w = width == null ? "100%" : width,
      // Started with http://james.padolsey.com/javascript/get-document-height-cross-browser/
      // to determine page height across browsers. Turns out that in our case, we can get by with
      // document.body.offsetHeight and document.body.clientHeight. Those two return the proper
      // even when the dom shrinks. Tested on Chrome, Safari, IE8, and Firefox
      D = document,
      h = Math.max(D.body.offsetHeight, D.body.clientHeight);
      if(h===0){
          h = Math.max(
              D.body.scrollHeight, D.documentElement.scrollHeight,
              D.body.offsetHeight, D.documentElement.offsetHeight,
              D.body.clientHeight, D.documentElement.clientHeight
          );
      }
      return {w: w, h: h};
    }

  });

});
