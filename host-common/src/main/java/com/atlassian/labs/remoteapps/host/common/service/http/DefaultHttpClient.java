package com.atlassian.labs.remoteapps.host.common.service.http;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.api.service.http.HttpClient;
import com.atlassian.labs.remoteapps.api.service.http.Response;
import com.atlassian.labs.remoteapps.api.service.http.ResponsePromise;
import com.atlassian.util.concurrent.ThreadFactories;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ConnectionReleaseTrigger;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.impl.nio.conn.AsyncSchemeRegistryFactory;
import org.apache.http.impl.nio.conn.PoolingClientAsyncConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.atlassian.labs.remoteapps.api.service.http.ResponsePromises.toResponsePromise;

public class DefaultHttpClient extends AbstractHttpClient implements HttpClient, DisposableBean
{
    private final HttpAsyncClient httpClient;
    private final Logger log = LoggerFactory.getLogger(DefaultHttpClient.class);
    private final RequestKiller requestKiller;
    private EventPublisher eventPublisher;

    public DefaultHttpClient(RequestKiller requestKiller, EventPublisher eventPublisher)
    {
        this.requestKiller = requestKiller;
        this.eventPublisher = eventPublisher;

        DefaultHttpAsyncClient client;
        try
        {
            IOReactorConfig ioReactorConfig = new IOReactorConfig();
            ioReactorConfig.setIoThreadCount(10);
            ioReactorConfig.setSelectInterval(100);
            ioReactorConfig.setInterestOpQueued(true);
            DefaultConnectingIOReactor reactor = new DefaultConnectingIOReactor(
                ioReactorConfig,
                ThreadFactories.namedThreadFactory("ra-async-http",
                    ThreadFactories.Type.DAEMON));
            reactor.setExceptionHandler(new IOReactorExceptionHandler()
            {
                @Override
                public boolean handle(IOException ex)
                {
                    log.error("IO exception in reactor", ex);
                    return false;
                }

                @Override
                public boolean handle(RuntimeException ex)
                {
                    log.error("Fatal runtime error", ex);
                    return false;
                }
            });
            client = new DefaultHttpAsyncClient(new PoolingClientAsyncConnectionManager(reactor, AsyncSchemeRegistryFactory.createDefault(), 3, TimeUnit.SECONDS)
            {
                @Override
                protected void finalize() throws Throwable
                {
                    // prevent the PoolingClientAsyncConnectionManager from logging - this causes exceptions due to
                    // the ClassLoader probably having been removed when the plugin shuts down.  Added a
                    // PluginEventListener to make sure the shutdown method is called while the plugin classloader
                    // is still active.
                }
            });
        }
        catch (IOReactorException e)
        {
            throw new RuntimeException("Reactor not set up correctly", e);
        }

        HttpParams params = client.getParams();
        // @todo add plugin version to UA string
        HttpProtocolParams.setUserAgent(params, "Atlassian-RemoteApps");

        HttpConnectionParams.setConnectionTimeout(params, 3 * 1000);
        HttpConnectionParams.setSoTimeout(params, 7 * 1000);
        HttpConnectionParams.setSocketBufferSize(params, 8 * 1024);
        HttpConnectionParams.setTcpNoDelay(params, true);

        ProxySelectorAsyncRoutePlanner routePlanner = new ProxySelectorAsyncRoutePlanner(
            client.getConnectionManager().getSchemeRegistry(),
            ProxySelector.getDefault());
        client.setRoutePlanner(routePlanner);

        httpClient = client;
        httpClient.start();
    }

    protected ResponsePromise execute(final DefaultRequest request)
    {
        // validate the request state
        request.validate();

        // trace the request if debugging is enabled; may be expensive
        if (log.isDebugEnabled())
        {
            //log.debug(request.dump());
        }

        // freeze the request state to prevent further mutability as we go to execute the request
        request.freeze();

        final long start = System.currentTimeMillis();
        final HttpRequestBase op;
        final String uri = request.getUri().toString();
        DefaultRequest.Method method = request.getMethod();
        switch (method)
        {
            case GET:       op = new HttpGet(uri);       break;
            case POST:      op = new HttpPost(uri);      break;
            case PUT:       op = new HttpPut(uri);       break;
            case DELETE:    op = new HttpDelete(uri);    break;
            case OPTIONS:   op = new HttpOptions(uri);   break;
            case HEAD:      op = new HttpHead(uri);      break;
            case TRACE:     op = new HttpTrace(uri) ;    break;
            default: throw new UnsupportedOperationException(method.toString());
        }
        if (request.hasEntity())
        {
            if (op instanceof HttpEntityEnclosingRequestBase)
            {
                byte[] entity = request.getEntityBytes();
                if (entity != null)
                {
                    ((HttpEntityEnclosingRequestBase) op).setEntity(new ByteArrayEntity(entity));
                }
                else
                {
                    int length = -1;
                    InputStream entityStream = request.getEntityStream();
                    if (entityStream instanceof ByteArrayInputStream)
                    {
                        try
                        {
                            length = entityStream.available();
                        }
                        catch (IOException e)
                        {
                            throw new RuntimeException("Will never happen", e);
                        }
                    }
                    ((HttpEntityEnclosingRequestBase) op).setEntity(new InputStreamEntity(entityStream, length));
                }
            }
            else
            {
                throw new UnsupportedOperationException("HTTP method " + method + " does not support sending an entity");
            }
        }

        for (Map.Entry<String, String> entry : request.getHeaders().entrySet())
        {
            op.setHeader(entry.getKey(), entry.getValue());
        }

        HttpContext localContext = new BasicHttpContext();
        final SettableFuture<Response> future = SettableFuture.create();
        FutureCallback<HttpResponse> futureCallback = new FutureCallback<HttpResponse>()
        {
            @Override
            public void completed(HttpResponse httpResponse)
            {
                requestKiller.completedRequest(op);
                long elapsed = System.currentTimeMillis() - start;
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode >= 200 && statusCode < 300)
                {
                    eventPublisher.publish(new HttpRequestCompletedEvent(uri, statusCode, elapsed, request.getAttributes()));
                }
                else
                {
                    eventPublisher.publish(new HttpRequestFailedEvent(uri, statusCode, elapsed, request.getAttributes()));
                }
                try
                {
                    DefaultResponse response = translate(httpResponse);

                    // trace the response if debugging is enabled; may be expensive
                    if (log.isDebugEnabled())
                    {
                        //log.debug(response.dump());
                    }

                    response.freeze();
                    future.set(response);
                }
                catch (IOException ex)
                {
                    this.failed(ex);
                }
            }

            @Override
            public void failed(Exception ex)
            {
                requestKiller.completedRequest(op);
                long elapsed = System.currentTimeMillis() - start;
                eventPublisher.publish(new HttpRequestFailedEvent(uri, ex.toString(), elapsed, request.getAttributes()));
                future.setException(ex);
            }

            @Override
            public void cancelled()
            {
                requestKiller.completedRequest(op);
                TimeoutException ex = new TimeoutException();
                long elapsed = System.currentTimeMillis() - start;
                eventPublisher.publish(new HttpRequestCancelledEvent(uri, ex.toString(), elapsed, request.getAttributes()));
                future.setException(ex);
            }
        };

        requestKiller.registerRequest(new NotifyingAbortableHttpRequest(op, futureCallback), 10);
        httpClient.execute(op, localContext, futureCallback);
        return toResponsePromise(future);
    }

    @Override
    public void destroy() throws Exception
    {
        httpClient.getConnectionManager().shutdown();
    }

    private DefaultResponse translate(HttpResponse httpResponse)
        throws IOException
    {
        StatusLine status = httpResponse.getStatusLine();
        DefaultResponse response = new DefaultResponse();
        response.setStatusCode(status.getStatusCode());
        response.setStatusText(status.getReasonPhrase());
        Header[] httpHeaders = httpResponse.getAllHeaders();
        for (Header httpHeader : httpHeaders)
        {
            response.setHeader(httpHeader.getName(), httpHeader.getValue());
        }
        HttpEntity entity = httpResponse.getEntity();
        if (entity != null)
        {
            response.setEntityStream(entity.getContent());
        }
        return response;
    }

    /**
     * This is a huge hack because the httpclient async lib doesn't seem to support aborting
     * requests
     */
    private class NotifyingAbortableHttpRequest implements AbortableHttpRequest
    {
        private final AbortableHttpRequest delegate;
        private final FutureCallback<HttpResponse> callback;

        private NotifyingAbortableHttpRequest(AbortableHttpRequest delegate, FutureCallback<HttpResponse> callback)
        {
            this.delegate = delegate;
            this.callback = callback;
        }

        @Override
        public void setConnectionRequest(ClientConnectionRequest connRequest) throws IOException
        {
            delegate.setConnectionRequest(connRequest);
        }

        @Override
        public void setReleaseTrigger(ConnectionReleaseTrigger releaseTrigger) throws IOException
        {
            delegate.setReleaseTrigger(releaseTrigger);
        }

        @Override
        public void abort()
        {
            delegate.abort();
            // workaround as this doesn't seem to be getting called during an abort.  In fact,
            // the request doesn't seem to be killed at all.  Note, this means the remote server
            // is perodically sending back data enough to evade the socket timeout and has
            // instead triggered the request killer.
            callback.cancelled();
        }

    }
}
