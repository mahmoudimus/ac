package it.confluence;

import com.atlassian.confluence.api.service.exceptions.ServiceException;
import com.atlassian.confluence.rest.serialization.CustomSerializerModuleFactory;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.multipart.impl.MultiPartWriter;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.Module;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.asList;

/**
 * <p>
 * Factory for creating jersey Client instances that can deserialize json
 * returned from the Confluence REST API into confluence java api model objects.
 * </p><p>
 * Example usage :
 * </p>
 * <pre>
 *     Client client = RestClientFactory.newClient();
 * </pre>
 * <p>
 * Consumers who need to register additional jersey providers might do the following :
 * </p>
 *
 * <pre>
 *      JacksonJsonProvider defaultProvider = RestClientFactory.createProvider(RestClientFactory.createJacksonModule());
 *      MyDelegatingProvider extraProvider = new MyDelegatingProvider(defaultProvider);
 *      Client client = newClient(defaultProvider, extraProvider);
 * </pre>
 *
 * <b>Note: This is a port of the Confluence REST Client factory yet to hit confluence master, this class should be
 * removed once RestClientFactory code is in confluence-rest-client</b>
 */
public class ConfluenceRestClientFactory
{
    /**
     * Create a client with default configuration suitable for remotely communicating with the
     * Confluence REST API.
     *
     * @return a new jersey client that can serialize and deserialize confluence model objects.
     */
    public static Client newClient()
    {
        return newClient(createJacksonModule());
    }

    public static Client newClient(Module jacksonModule)
    {
        checkNotNull(jacksonModule);
        Object provider = createProvider(jacksonModule);
        ClientConfig config = createConfig(provider);
        return Client.create(config);
    }

    public static Client newClient(Object provider, Object... providers)
    {
        checkNotNull(provider);

        ClientConfig clientConfig = createConfig(asList(provider, providers).toArray());
        return Client.create(clientConfig);
    }

    /**
     * Helper method to create a jersey client config with the given providers registered.
     *
     * @param providers instances of providers annotated with {@link javax.ws.rs.ext.Provider}
     */
    public static ClientConfig createConfig(Object... providers)
    {
        ClientConfig clientConfig = new DefaultClientConfig();
        for (Object provider : providers)
        {
            // check that the provider is annotated with @Provider somewhere in it's hierarchy
            Class clazz = provider.getClass();
            while(!Object.class.equals(clazz)){
                if (clazz.isAnnotationPresent(Provider.class))
                    break;

                clazz = clazz.getSuperclass();
            }
            if (clazz.equals(Object.class))
                throw new IllegalArgumentException(provider.getClass() + " is not annotated with "+Provider.class);

            clientConfig.getSingletons().add(provider);
        }
        clientConfig.getClasses().add(MultiPartWriter.class);
        return clientConfig;
    }

    /**
     * @return the serialization module for serializing and deserializing java model objects.
     */
    public static Module createJacksonModule()
    {
        return CustomSerializerModuleFactory.create();
    }

    /**
     * Creates {@link javax.ws.rs.ext.Provider} instance to perform custom serialization needed for
     * using the remote API model objects.  Registers the jacksonModule with the provider.
     *
     * @param jacksonModule custom serialization module to register with the provider
     */
    public static JacksonJsonProvider createProvider(Module jacksonModule)
    {
        JacksonJsonProvider provider = new JacksonJaxbJsonProvider()
        {
            @Override
            public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
                    throws IOException
            {
                try
                {
                    final int buffersize_512kB = 1 << 19;  // because I reckon that'll do
                    entityStream = new ReusableBufferedInputStream(entityStream);
                    entityStream.mark(buffersize_512kB); // buffer the json stream to be able to rollback the stream if it chokes
                    return super.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
                }
                catch(JsonProcessingException ex)
                {
                    entityStream.reset();
                    Writer writer = new StringWriter();
                    IOUtils.copy(entityStream, writer);
                    throw new ServiceException("Json process exception encountered in client reading entity stream : "+writer.toString(), ex);
                }
            }
        };
        provider.configure(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES, false);
        if (jacksonModule != null)
        {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(jacksonModule);
            provider.setMapper(mapper);
        }
        return provider;
    }

    /**
     * Decorated subclass of {@link java.io.BufferedInputStream} that can be read multiple times as it marks the stream
     * with {@link Integer#MAX_VALUE} and resets the stream when {@code close()} is called.
     */
    private static class ReusableBufferedInputStream extends BufferedInputStream
    {
        public ReusableBufferedInputStream(InputStream inputStream)
        {
            super(inputStream);
            super.mark(Integer.MAX_VALUE);
        }

        /**
         * Calls {@link java.io.BufferedInputStream#reset()} instead of closing the stream.
         * @throws IOException
         */
        @Override
        public void close() throws IOException
        {
            super.reset();
        }

        /**
         * Actually closes the ReusableBufferedInputStream by calling {@link java.io.BufferedInputStream#close()}.
         * @throws IOException
         */
        public void destroy() throws IOException
        {
            super.close();
        }
    }
}
