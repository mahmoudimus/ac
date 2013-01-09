package com.atlassian.plugin.remotable.plugin.util.http.bigpipe;

import com.atlassian.plugin.remotable.api.service.http.bigpipe.BigPipe;
import com.atlassian.plugin.remotable.api.service.http.bigpipe.RequestIdAccessor;
import com.atlassian.security.random.SecureRandomFactory;
import com.atlassian.util.concurrent.CopyOnWriteMap;
import com.atlassian.util.concurrent.Promise;
import com.google.common.util.concurrent.FutureCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Maps.newHashMap;
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

    ScheduledExecutorService cleanupThread = Executors.newSingleThreadScheduledExecutor();

    private final Map<String, RequestContentSet> requestContentSets = CopyOnWriteMap.newHashMap();

    public BigPipeImpl()
    {
        cleanupThread.scheduleAtFixedRate(new Runnable()
        {
            @Override
            public void run()
            {
                cleanExpiredRequests();
            }
        }, 2, 1, TimeUnit.MINUTES);
    }

    private void cleanExpiredRequests()
    {
        for (RequestContentSet contentSet : requestContentSets.values())
        {
            if (contentSet.isExpired())
            {
                contentSet.removeRequestContentSet();
            }
        }
    }

    @Override
    public RequestIdAccessor getRequestIdAccessor()
    {
        return requestIdAccessor;
    }

    @Override
    public String registerContentPromise(String requestId, Promise<String> responsePromise)
    {
        // give each content its own unique id for replacing later
        String contentId = String.valueOf(secureRandom.nextLong());
        registerContentPromise(requestId, contentId, responsePromise);
        return contentId;
    }

    @Override
    public void registerContentPromise(final String requestId, final String contentId, Promise<String> responsePromise)
    {
        RequestContentSet requestContentSet = requestContentSets.get(requestId);
        if (requestContentSet == null)
        {
            requestContentSet = new RequestContentSet(requestId);
            requestContentSets.put(requestId, requestContentSet);
        }

        final RequestContentSet finalRequestContentSet = requestContentSet;
        responsePromise.then(new FutureCallback<String>()
        {
            @Override
            public void onSuccess(String result)
            {
                finalRequestContentSet.notifyConsumers();
            }

            @Override
            public void onFailure(Throwable t)
            {
                finalRequestContentSet.notifyConsumers();
            }
        });
        InternalHandler handler = new InternalHandler(contentId, requestContentSet, responsePromise);
        requestContentSet.addHandler(handler);

    }

    @Override
    public String waitForContent(String requestId)
    {
        RequestContentSet request = requestContentSets.get(requestId);
        return convertContentToJson(
                request != null ? request.waitForFinishedContent() : Collections.<String, String>emptyMap());
    }

    @Override
    public String consumeContent(String requestId)
    {
        RequestContentSet request = requestContentSets.get(requestId);
        return convertContentToJson(
                request != null ? request.consumeFinishedContent() : Collections.<String, String>emptyMap());
    }

    private String convertContentToJson(Map<String,String> set)
    {
        JSONArray array = new JSONArray();
        for (Map.Entry<String,String> entry : set.entrySet())
        {
            String contentId = entry.getKey();
            String html = entry.getValue();
            JSONObject content = new JSONObject();
            try
            {
                content.put("id", contentId);
                content.put("html", html);
            }
            catch (JSONException e)
            {
                throw new IllegalArgumentException("Unable to serialize content", e);
            }
            array.put(content);
        }

        return array.toString();
    }

    /**
     * Manages individual bit of content
     */
    private class InternalHandler
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

        public String getContentId()
        {
            return contentId;
        }

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

        public Map<String,String> consumeFinishedContent()
        {
            Map<String, String> result = newHashMap();
            synchronized (lock)
            {
                if (handlers.isEmpty())
                {
                    return Collections.emptyMap();
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
        public Map<String,String> waitForFinishedContent()
        {
            Map<String, String> result = newHashMap();
            synchronized (lock)
            {
                if (handlers.isEmpty())
                {
                    return Collections.emptyMap();
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

        private void removeAllFinishedHandlers(Map<String, String> result)
        {
            for (InternalHandler handler : newHashSet(handlers.values()))
            {
                if (handler.isFinished())
                {
                    removeContent(handler.getContentId());
                    result.put(handler.contentId, handler.getContent().claim());
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
