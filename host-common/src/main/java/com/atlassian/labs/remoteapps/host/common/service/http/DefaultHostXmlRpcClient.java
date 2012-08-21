package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.labs.remoteapps.api.service.http.*;
import com.atlassian.labs.remoteapps.api.service.http.XmlRpcException;
import com.atlassian.labs.remoteapps.api.service.http.XmlRpcFault;
import com.atlassian.labs.remoteapps.spi.WrappingPromise;
import com.atlassian.xmlrpc.BindingException;
import com.atlassian.xmlrpc.ServiceObject;
import com.atlassian.xmlrpc.XmlRpcClientProvider;
import com.atlassian.xmlrpc.XmlRpcInvocationHandler;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import redstone.xmlrpc.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Proxy;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

/**
 * Helps make authenticated xmlrpc calls
 */
public class DefaultHostXmlRpcClient implements HostXmlRpcClient
{
    private final static String serverXmlRpcPath = "/rpc/xmlrpc";

    private final XmlRpcSerializer serializer = new XmlRpcSerializer();

    private final HostHttpClient hostHttpClient;


    public DefaultHostXmlRpcClient(HostHttpClient hostHttpClient)
    {
        this.hostHttpClient = hostHttpClient;
    }

    @Override
    public <T> T bind(Class<T> bindClass)
    {
        if (!bindClass.isInterface())
        {
            throw new IllegalArgumentException("Class " + bindClass.getName() + "is not an interface");
        }
        ServiceObject serviceObject = bindClass.getAnnotation(ServiceObject.class);
        if (serviceObject == null)
        {
            throw new IllegalArgumentException("Could not find ServiceObject annotation on " + bindClass.getName());
        }

        XmlRpcInvocationHandler handler = new XmlRpcInvocationHandler(new XmlRpcClientProvider()
        {
            public Object execute(String serviceName, String methodName, Vector arguments) throws BindingException
            {
                try
                {
                    return invoke(serviceName + "." + methodName, Object.class, arguments.toArray()).get();
                }
                catch (InterruptedException e)
                {
                    throw new BindingException(e);
                }
                catch (ExecutionException e)
                {
                    throw new BindingException(e.getCause());
                }
            }
        });

        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{bindClass},
                handler);
    }

    @Override
    public <T> Promise<T> invoke(String method, Class<T> resultType, Object... arguments)
    {
        Writer writer = new StringWriter(2048);
        beginCall(writer, method);

        FaultHandlingXmlRpcParser parser = new FaultHandlingXmlRpcParser();
        for (Object argument : arguments)
        {
            try
            {
                writer.write("<param>");
                serializer.serialize(argument, writer);
                writer.write("</param>");
            }
            catch (IOException ioe)
            {
                throw new XmlRpcException(
                        XmlRpcMessages.getString("XmlRpcClient.NetworkError"), ioe);
            }
        }

        return endCall(writer, parser, resultType);
    }

    /**
     * Initializes the XML buffer to be sent to the server with the XML-RPC content common to all
     * method calls, or serializes it directly over the writer if streaming is used. The parameters
     * to the call are added in the execute() method, and the closing tags are appended when the
     * call is finalized in endCall().
     *
     * @param methodName The name of the method to call.
     */

    private void beginCall(Writer writer, String methodName) throws XmlRpcException
    {
        try
        {
            ((StringWriter) writer).getBuffer().setLength(0);

            writer.write("<?xml version=\"1.0\" encoding=\"");
            writer.write(XmlRpcMessages.getString("XmlRpcClient.Encoding"));
            writer.write("\"?>");
            writer.write("<methodCall><methodName>");
            writer.write(methodName);
            writer.write("</methodName><params>");
        }
        catch (IOException ioe)
        {
            throw new XmlRpcException(
                    XmlRpcMessages.getString("XmlRpcClient.NetworkError"), ioe);
        }
    }


    /**
     * Finalizaes the XML buffer to be sent to the server, and creates a HTTP buffer for the call.
     * Both buffers are combined into an XML-RPC message that is sent over a socket to the server.
     *
     * @return The parsed return value of the call.
     * @throws XmlRpcException when some IO problem occur.
     */

    private <T> Promise<T> endCall(Writer writer, final FaultHandlingXmlRpcParser parser,
            final Class<T> castResultTo)
    {
        final SettableFuture<T> future = SettableFuture.create();
        try
        {
            writer.write("</params>");
            writer.write("</methodCall>");

            Futures.addCallback(hostHttpClient.post(serverXmlRpcPath, "text/xml; charset=" +
                    XmlRpcMessages.getString("XmlRpcClient.Encoding"), writer.toString()),
                    new FutureCallback<Response>()
                    {
                        @Override
                        public void onSuccess(Response result)
                        {
                            try
                            {
                                parser.parse(new BufferedInputStream(result.getEntityStream()));
                            }
                            catch (Exception e)
                            {
                                future.setException(new XmlRpcException(
                                        XmlRpcMessages.getString("XmlRpcClient.ParseError"), e));
                            }

                            if (parser.isFaultResponse())
                            {
                                XmlRpcStruct fault = (XmlRpcStruct) parser.getParsedValue();

                                future.setException(new XmlRpcFault(fault.getInteger("faultCode"),
                                        fault.getString("faultString")));
                            }
                            else
                            {
                                Object parsedValue = parser.getParsedValue();
                                if (parsedValue != null && castResultTo.isAssignableFrom(parsedValue.getClass()))
                                {
                                    future.set(castResultTo.cast(parsedValue));
                                }
                                else
                                {
                                    future.setException(new XmlRpcException("Unexpected return type: " + parsedValue));
                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable t)
                        {
                            future.setException(t);
                        }
                    });
        }
        catch (IOException ioe)
        {
            throw new RuntimeException("Should never happen", ioe);
        }
        return new WrappingPromise<T>(future);
    }
}
