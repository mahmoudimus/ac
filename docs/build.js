var fs = require('fs');
var harpGlobals = require('./harp.json');
var jiraSchema = require('../plugin/target/classes/schema/jira-schema.json');
var confluenceSchema = require('../plugin/target/classes/schema/confluence-schema.json');

var keys = Object.keys(jiraSchema.properties.capabilities.properties).sort();
var obj = {};
for(var i = 0; i < keys.length; i++) {
    obj[keys[i]] = jiraSchema.properties.capabilities.properties[keys[i]];
}
jiraSchema.properties.capabilities.properties = obj;
harpGlobals.globals.jira = jiraSchema;
harpGlobals.globals.confluence = confluenceSchema;

fs.writeFile('./harp.json', JSON.stringify(harpGlobals,null,2), function(err) {
    if(err) {
        console.log(err);
    } else {
        console.log("Globals written");
    }
});
