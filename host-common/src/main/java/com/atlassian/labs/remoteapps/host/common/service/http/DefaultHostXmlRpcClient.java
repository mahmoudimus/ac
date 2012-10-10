package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.labs.remoteapps.api.Deferred;
import com.atlassian.labs.remoteapps.api.Deferreds;
import com.atlassian.labs.remoteapps.api.service.http.HostHttpClient;
import com.atlassian.labs.remoteapps.api.service.http.HostXmlRpcClient;
import com.atlassian.labs.remoteapps.api.service.http.Response;
import com.atlassian.labs.remoteapps.api.service.http.XmlRpcException;
import com.atlassian.labs.remoteapps.api.service.http.XmlRpcFault;
import com.atlassian.plugin.util.ChainingClassLoader;
import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.xmlrpc.BindingException;
import com.atlassian.xmlrpc.ServiceObject;
import com.atlassian.xmlrpc.XmlRpcClientProvider;
import redstone.xmlrpc.XmlRpcMessages;
import redstone.xmlrpc.XmlRpcSerializer;
import redstone.xmlrpc.XmlRpcStruct;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Vector;

import static java.lang.System.*;

/**
 * Helps make authenticated xmlrpc calls
 */
public class DefaultHostXmlRpcClient implements HostXmlRpcClient
{
    private final static URI serverXmlRpcPath = URI.create("/rpc/xmlrpc");

    private final XmlRpcSerializer serializer = new XmlRpcSerializer();

    private final HostHttpClient httpClient;


    public DefaultHostXmlRpcClient(HostHttpClient httpClient)
    {
        this.httpClient = httpClient;
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

        PromiseAwareXmlRpcInvocationHandler handler = new PromiseAwareXmlRpcInvocationHandler(new XmlRpcClientProvider()
        {
            public Object execute(String serviceName, String methodName, Vector arguments) throws BindingException
            {
                Object[] argsWithToken = new Object[arguments.size() + 1];
                argsWithToken[0] = "";
                arraycopy(arguments.toArray(), 0, argsWithToken, 1, arguments.size());
                return invoke(serviceName + "." + methodName, Object.class, argsWithToken);
            }
        });

        return (T) Proxy.newProxyInstance(new ChainingClassLoader(getClass().getClassLoader(), bindClass.getClassLoader()), new Class[]{bindClass},
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
     * Finalizes the XML buffer to be sent to the server, and creates a HTTP buffer for the call.
     * Both buffers are combined into an XML-RPC message that is sent over a socket to the server.
     *
     * @return The parsed return value of the call.
     * @throws XmlRpcException when some IO problem occur.
     */
    private <T> Promise<T> endCall(Writer writer, final FaultHandlingXmlRpcParser parser,
            final Class<T> castResultTo)
    {
        final Deferred<T> deferred = Deferred.create();
        try
        {
            writer.write("</params>");
            writer.write("</methodCall>");

            httpClient
                .newRequest(serverXmlRpcPath)
                .setContentType("text/xml")
                .setContentCharset(XmlRpcMessages.getString("XmlRpcClient.Encoding"))
                .setEntity(writer.toString())
                .post()
                .ok(new Effect<Response>()
                {
                    @Override
                    public void apply(Response response)
                    {
                        try
                        {
                            parser.parse(new BufferedInputStream(response.getEntityStream()));
                        }
                        catch (Exception e)
                        {
                            deferred.reject(new XmlRpcException(
                                XmlRpcMessages.getString("XmlRpcClient.ParseError"), e));
                        }

                        if (parser.isFaultResponse())
                        {
                            XmlRpcStruct fault = (XmlRpcStruct) parser.getParsedValue();
                            deferred.reject(new XmlRpcFault(fault.getInteger("faultCode"),
                                fault.getString("faultString")));
                        }
                        else
                        {
                            Object parsedValue = parser.getParsedValue();
                            if (parsedValue != null && castResultTo.isAssignableFrom(parsedValue.getClass()))
                            {
                                deferred.resolve(castResultTo.cast(parsedValue));
                            } else
                            {
                                deferred.reject(new XmlRpcException("Unexpected return type: " + parsedValue));
                            }
                        }
                    }
                })
                // since xmlrpc should always return 200 OK responses unless an error has occurred,
                // treat all other response codes as errors
                .otherwise(Deferreds.reject(deferred));
        }
        catch (IOException ioe)
        {
            throw new RuntimeException("Should never happen", ioe);
        }
        return deferred.promise();
    }
}
