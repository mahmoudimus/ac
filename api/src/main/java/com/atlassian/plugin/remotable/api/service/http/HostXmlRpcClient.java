package com.atlassian.plugin.remotable.api.service.http;

import com.atlassian.util.concurrent.Promise;

/**
 * Makes xml-rpc calls to the host using the {@link HostHttpClient}
 */
public interface HostXmlRpcClient
{
    /**
     *  Invokes a method on the terminating XML-RPC end point. The supplied method name and
     *  argument collection is used to encode the call into an XML-RPC compatible message.
     *
     *  @param method The name of the method to call.
     *
     *  @param arguments The arguments to encode in the call.
     *
     *  @return The object returned from the terminating XML-RPC end point.
     */
    <T> Promise<T> invoke(String method, Class<T> resultType, Object... arguments);

    /**
     * Binds a class to create a nice typed interface to XML-RPC objects.
     *
     * See <a href="https://labs.atlassian.com/wiki/display/XMLRPC/Home">Atlassian XML-RPC</a>.
     * @param serviceClass An interface with specially annotated methods and objects.  An empty string will always be inserted
     *                   as the first argument to handle the token argument in Atlassian remote APIs
     * @param <T> The service interface
     * @return An implementation of the service interface that will make the calls in the background
     */
    <T> T bind(Class<T> serviceClass);

}
