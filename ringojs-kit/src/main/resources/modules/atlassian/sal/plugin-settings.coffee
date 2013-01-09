factory = appContext.getService "com.atlassian.plugin.remotable.api.service.PluginSettingsAsyncFactory"

{toArray} = require "vendor/underscore"

op = (method, takesValue) ->
  ->
    args = toArray arguments
    store =
      if args.length is (if takesValue then 3 else 2)
        key = args.unshift()
        factory.getSettingsForKey key
      else
        factory.getGlobalSettings()
    store[method] args...

# get(key)
#   gets a plugin-setting for the specified key in the global store
# get(entityKey, key)
#   gets a plugin-setting for the specified key in an entity-specific store
exports.get = op "get"

# put(key, value)
#   puts a plugin-setting for the specified key in the global store
# put(entityKey, key, value)
#   puts a plugin-setting for the specified key in an entity-specific store
exports.remove = op "remove"

# remove(key)
#   removes a plugin-setting for the specified key in the global store
# remove(entityKey, key)
#   removes a plugin-setting for the specified key in an entity-specific store
exports.put = op "put", true
