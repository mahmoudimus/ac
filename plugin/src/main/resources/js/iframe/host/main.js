/**
* confluence.web.resources:almond deliberately breaks AMD modules so they are exposed globally.
* Turn it back into a defined module so others can use it as AMD if they wish.
*/
define('connect-host', function(){
    return _AP;
});

require('ac/history');
require('ac/cookie');
require('ac/env');
require('ac/inline-dialog');
require('ac/dialog');
require('ac/messages');
require('ac/request');
