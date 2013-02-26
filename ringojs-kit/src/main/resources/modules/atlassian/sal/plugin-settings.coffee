factory = appContext.getService "com.atlassian.sal.api.pluginsettings.PluginSettingsFactory"

{toArray} = require "vendor/underscore"

# # get(key)
# #   gets a plugin-setting for the specified key in the global store
# # get(entityKey, key)
# #   gets a plugin-setting for the specified key in an entity-specific store

# # put(key, value)
# #   puts a plugin-setting for the specified key in the global store
# # put(entityKey, key, value)
# #   puts a plugin-setting for the specified key in an entity-specific store

# # remove(key)
# #   removes a plugin-setting for the specified key in the global store
# # remove(entityKey, key)
# #   removes a plugin-setting for the specified key in an entity-specific store

# A simple key value store targetted for plugin specific configuration
#
# @example
#    let ps = require('atlassian/sal/plugin-settings');
#    ps.put('key1', 'somevalue') // Set key and value
#    ps.get('key1') // get key's value
#
class PluginSettings
  # No need to create an instance. An instance is created when required.
  constructor: ->
    @factory = appContext.getService "com.atlassian.sal.api.pluginsettings.PluginSettingsFactory"

  # Gets a key from the datastore
  # @overload get(key)
  #   @param [String] key Name of the key
  # @overload get(entityKey ,key)
  #   @param [String] entityKey Name of the entityKey
  #   @param [String] key Name of the key
  get: (entityKey, key) ->
    args = toArray arguments
    store =
      if args.length is 2
        key = args.shift()
        @factory.createSettingsForKey key
      else
        @factory.createGlobalSettings()
    store.get args.pop()

  # Sets a key and its value
  # @overload put(key, value)
  #   @param [String] key Name of the key
  #   @param [Any] value Value of the key
  # @overload put(entityKey ,key)
  #   @param [String] entityKey Name of the entityKey. If not provided, value is stored globally
  #   @param [String] key Name of the key
  #   @param [Any] value Value of the key
  put: (entityKey, key, value) ->
    args = toArray arguments
    store =
      if args.length is 3
        key = args.shift()
        @factory.createSettingsForKey key
      else
        @factory.createGlobalSettings()
    val = args.pop()
    val = JSON.stringify(val) if typeof val == 'object' or typeof val == 'array'
    store.put args.pop(), val

  # Removes a key from the datastore
  # @overload remove(key)
  #   @param [String] key Name of the key
  # @overload remove(entityKey ,key)
  #   @param [String] entityKey Name of the entityKey
  #   @param [String] key Name of the key
  remove: (entityKey, key) ->
    args = toArray arguments
    store =
      if args.length is 2
        key = args.shift()
        @factory.createSettingsForKey key
      else
        @factory.createGlobalSettings()
    store.remove args.pop()

exports = module.exports = new PluginSettings