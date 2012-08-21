package com.atlassian.labs.remoteapps.api.service.http;

import com.atlassian.labs.remoteapps.api.Promise;

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
     *
     *  @throws XmlRpcException One or more of the supplied arguments are unserializable. That is,
     *                          the built-in serializer connot parse it or find a custom serializer
     *                          that can. There may also be problems with the socket communication.
     */
    <T> Promise<T> invoke(String method, Class<T> resultType, Object... arguments) throws XmlRpcException;

    /**
     * Binds a class to create a nice typed interface to XML-RPC objects.
     *
     * See <a href="https://labs.atlassian.com/wiki/display/XMLRPC/Home">Atlassian XML-RPC</a>.
     * @param serviceClass An interface with specially annotated methods and objects
     * @param <T> The service interface
     * @return An implementation of the service interface that will make the calls in the background
     */
    <T> T bind(Class<T> serviceClass);

}
