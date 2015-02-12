#!/usr/bin/env node

var marketplace = require('./marketplace'),
    _ = require('lodash'),
    util = require('util');

var count = 0;

marketplace.run({
    before: function (opts) {
        marketplace.requestQueue().drain = function () {
            console.log(count);
        }
    },
    marketplaceAddonCallback: function (addon, opts) {
        count++;
    }
});

