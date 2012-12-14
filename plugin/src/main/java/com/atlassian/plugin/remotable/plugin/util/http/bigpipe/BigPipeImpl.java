package com.atlassian.plugin.remotable.plugin.util.http.bigpipe;

import com.atlassian.plugin.remotable.spi.http.bigpipe.BigPipe;
import com.atlassian.plugin.remotable.spi.http.bigpipe.BigPipeContentHandler;
import com.atlassian.plugin.remotable.spi.http.bigpipe.RequestIdAccessor;
import com.atlassian.security.random.SecureRandomFactory;
import com.atlassian.util.concurrent.CopyOnWriteMap;
import com.atlassian.util.concurrent.ForwardingPromise;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Manages big pipe instances
 */
@Component
public final class BigPipeImpl implements BigPipe
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final SecureRandom secureRandom = SecureRandomFactory.newInstance();
    private final RequestIdAccessorImpl requestIdAccessor = new RequestIdAccessorImpl();

    // fixme: add big pipe cleaner thread to clean expired request sets
    private final Map<String, RequestContentSet> requestContentSets = CopyOnWriteMap.newHashMap();

    @Override
    public RequestIdAccessor getRequestIdAccessor()
    {
        return requestIdAccessor;
    }

    @Override
    public BigPipeContentHandler createContentHandler(String requestId, Promise<String> responsePromise)
    {
        // give each content its own unique id for replacing later
        String contentId = String.valueOf(secureRandom.nextLong());
        return createContentHandler(requestId, contentId, responsePromise);
    }

    @Override
    public BigPipeContentHandler createContentHandler(final String requestId, final String contentId, Promise<String> responsePromise)
    {
        final Promise<String> errorHandlingPromise = responsePromise.recover(new Function<Throwable, String>()
        {
            @Override
            public String apply(Throwable input)
            {
                log.debug("Error handling bigpipe request for id '" + requestId + "'", input);

                return "<span class=\"bp-error\">Error: " + input.getMessage() + "</span>";
            }
        });

        RequestContentSet requestContentSet = requestContentSets.get(requestId);
        if (requestContentSet == null)
        {
            requestContentSet = new RequestContentSet(requestId);
            requestContentSets.put(requestId, requestContentSet);
        }

        Promise<String> cleanedUpPromise = new CleanupPromise(errorHandlingPromise, requestContentSet, contentId);


        InternalHandler handler = new InternalHandler(contentId, requestContentSet, cleanedUpPromise);
        return requestContentSet.addHandler(handler);
    }

    @Override
    public Iterable<BigPipeContentHandler> waitForCompletedHandlers(String requestId)
    {
        RequestContentSet request = requestContentSets.get(requestId);
        if (request != null)
        {
            return request.waitForFinishedContent();
        }
        else
        {
            return Collections.emptyList();
        }
    }

    @Override
    public String convertContentHandlersToJson(Iterable<BigPipeContentHandler> handlers)
    {
        JSONArray result = new JSONArray();
        for (BigPipeContentHandler handler : handlers)
        {
            String html = handler.getContent().claim();
            JSONObject content = new JSONObject();
            try
            {
                content.put("id", handler.getContentId());
                content.put("html", html);
            }
            catch (JSONException e)
            {
                throw new IllegalArgumentException("Unable to serialize content", e);
            }
            result.put(content);
        }
        return result.toString();
    }

    public Iterable<BigPipeContentHandler> consumeCompletedHandlers(String requestId)
    {
        RequestContentSet request = requestContentSets.get(requestId);
        if (request != null)
        {
            return request.consumeFinishedContent();
        }
        else
        {
            return Collections.emptyList();
        }
    }

    private static class CleanupPromise extends ForwardingPromise<String>
    {
        private final Promise<String> promise;
        private final RequestContentSet requestContentSet;
        private final String contentId;

        public CleanupPromise(Promise<String> promise, RequestContentSet requestContentSet, String contentId)
        {
            this.promise = promise;
            this.requestContentSet = requestContentSet;
            this.contentId = contentId;
        }

        @Override
        protected Promise<String> delegate()
        {
            return promise;
        }

        @Override
        public String claim()
        {
            try
            {
                return super.claim();
            }
            finally
            {
                requestContentSet.removeContent(contentId);
            }
        }
    }

    /**
     * Manages individual bit of content
     */
    private class InternalHandler implements BigPipeContentHandler
    {
        private final String contentId;
        private final RequestContentSet request;
        private final Promise<String> responsePromise;

        public InternalHandler(String contentId, RequestContentSet requestContentSet, Promise<String> responsePromise)
        {
            this.contentId = contentId;
            this.request = requestContentSet;
            this.responsePromise = responsePromise.then(new FutureCallback<String>()
            {
                @Override
                public void onSuccess(String result)
                {
                    request.notifyConsumers();
                }

                @Override
                public void onFailure(Throwable t)
                {
                    request.notifyConsumers();
                }
            });
        }

        @Override
        public String getCurrentContent()
        {
            if (responsePromise.isDone())
            {
                return responsePromise.claim();
            }
            else
            {
                return "<span class=\"bp-" + contentId + " bp-loading\"></span>";
            }
        }

        @Override
        public String getContentId()
        {
            return contentId;
        }

        @Override
        public Promise<String> getContent()
        {
            return responsePromise;
        }

        public boolean isFinished()
        {
            return responsePromise.isDone();
        }
    }

    /**
     * Manages a set of content for a single page.  Content for the page can be waited upon in a blocking call to be
     * woken up when new content is available.
     */
    private class RequestContentSet
    {
        private final Map<String, InternalHandler> handlers = CopyOnWriteMap.newHashMap();

        /**
         * The expiration of the page content in the case where the xhr call to retrieve the content is never made.
         */
        private final long expiry;
        private final String requestId;
        private final Object lock = new Object();

        public RequestContentSet(String requestId)
        {
            this.requestId = requestId;
            this.expiry = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);
        }

        /**
         * Ensures only one handler per contentid
         *
         * @return The handler to actually use
         */
        public InternalHandler addHandler(InternalHandler handler)
        {
            if (!handlers.containsKey(handler.contentId))
            {
                handlers.put(handler.contentId, handler);
                return handler;
            }
            else
            {
                log.debug("Content id '{}' already assigned for request '{}'", handler.contentId, requestId);
                return handlers.get(handler.contentId);
            }
        }

        public void removeContent(String contentId)
        {
            handlers.remove(contentId);
        }

        public Iterable<BigPipeContentHandler> consumeFinishedContent()
        {
            List<BigPipeContentHandler> result = newArrayList();
            synchronized (lock)
            {
                if (handlers.isEmpty())
                {
                    return Collections.emptyList();
                }
                removeAllFinishedHandlers(result);
                if (!hasMoreContent())
                {
                    // we've expired
                    log.info("All content has been consumed for request id {}", requestId);
                    removeRequestContentSet();
                }
            }
            return result;
        }

        private void removeRequestContentSet()
        {
            handlers.clear();
            requestContentSets.remove(requestId);
        }

        private boolean hasMoreContent()
        {
            return !handlers.isEmpty();
        }

        /**
         * Waits for content to be finished and removes it from the set of outstanding content if done.  It will only
         * wait until the timeout for the page has been reached.
         */
        public Iterable<BigPipeContentHandler> waitForFinishedContent()
        {
            List<BigPipeContentHandler> result = newArrayList();
            synchronized (lock)
            {
                if (handlers.isEmpty())
                {
                    return Collections.emptyList();
                }
                removeAllFinishedHandlers(result);
                if (result.isEmpty() && !isExpired())
                {
                    try
                    {
                        long timeout = expiry - System.currentTimeMillis();
                        if (timeout > 0)
                        {
                            lock.wait(timeout);
                        }
                        removeAllFinishedHandlers(result);
                    }
                    catch (InterruptedException e)
                    {
                        // ignore
                    }
                }
            }

            if (isExpired())
            {
                // we've expired
                log.info("Timeout waiting for {} jobs for request id {}", handlers.size(), requestId);

                removeRequestContentSet();
            }

            return result;
        }

        private void removeAllFinishedHandlers(List<BigPipeContentHandler> result)
        {
            for (InternalHandler handler : newHashSet(handlers.values()))
            {
                if (handler.isFinished())
                {
                    removeContent(handler.contentId);
                    result.add(handler);
                }
            }
        }

        public boolean isExpired()
        {
            return System.currentTimeMillis() >= expiry;
        }

        public void notifyConsumers()
        {
            synchronized (lock)
            {
                lock.notifyAll();
            }
        }
    }
}
