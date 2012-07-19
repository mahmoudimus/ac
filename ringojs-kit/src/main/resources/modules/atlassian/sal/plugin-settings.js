var factory = appContext.getService("com.atlassian.labs.remoteapps.api.services.PluginSettingsAsyncFactory");

function getStore(args) {
  if (args.length == 2) {
    return [factory.getSettingsForKey(args[0]), args[1]];
  } else {
    return [factory.getGlobalSettings(), args[1]];
  }
}

exports.get = function(entityKey, key) {
  var [store, propKey] = getStore(arguments);
  return store.get(propKey);
};
exports.remove = function(entityKey, key) {
  var [store, propKey] = getStore(arguments);
  return store.remove(propKey);
};
exports.put = function(entityKey, key) {
  var [store, propKey] = getStore(arguments);
  return store.put(propKey);
};
