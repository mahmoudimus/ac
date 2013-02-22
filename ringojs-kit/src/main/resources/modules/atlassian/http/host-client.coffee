# Used to make requests back to the host application.  Implementations handle
# oauth signing and user propagation.  URIs should be relative to the host app url,
# including the context path (e.g. relative to something like http://localhost:2990/jira).
class HostHttpClient
  # No need to create an instance of HostHttpClient. An instance is created when required.
  constructor: ->
    return appContext.getBean "hostHttpClient"

  # Runs the provided callable code in a RequestContext with the specified clientKey and userId.
  #
  # @param [String] clientKey The clientKey to call as
  # @param [String] userId The user id to call as
  # @param [Function] callable The executable code to call
  # @return The value returned from the callable code
  callAs: (clientKey, userId, callable)->

module.exports = new HostHttpClient
