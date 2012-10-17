package com.atlassian.plugin.remotable.plugin.util.http.bigpipe;

import com.atlassian.security.random.SecureRandomFactory;
import com.atlassian.util.concurrent.CopyOnWriteMap;
import com.google.common.base.Function;
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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Manages big pipe instances
 */
@Component
public class BigPipe
{
    private final SecureRandom secureRandom = SecureRandomFactory.newInstance();
    private static final Logger log = LoggerFactory.getLogger(BigPipe.class);

    private final Map<String,RequestContentSet> requestContentSets = CopyOnWriteMap.newHashMap();

    public BigPipeContentHandler createContentHandler(String requestId, Function<String,String> successFunction,
                                                         Function<Throwable, String> failureFunction)
    {
        // give each content its own unique id for replacing later
        String contentId = String.valueOf(secureRandom.nextLong());
        return createContentHandler(requestId, contentId, successFunction, failureFunction);
    }
    public BigPipeContentHandler createContentHandler(String requestId, String contentId, Function<String,String> successFunction,
                                                             Function<Throwable, String> failureFunction)
    {
        if (failureFunction == null)
        {
            failureFunction = new Function<Throwable, String>()
            {
                @Override
                public String apply(Throwable input)
                {
                    return "<span class=\"bp-error\">Error: " + input.getMessage() + "</span>";
                }
            };
        }
        checkNotNull(successFunction);
        RequestContentSet request = requestContentSets.get(requestId);
        if (request == null)
        {
            request = new RequestContentSet(requestId);
            requestContentSets.put(requestId, request);
        }

        InternalHandler handler = new InternalHandler(requestId, contentId,
                request, successFunction, failureFunction);
        return request.addHandler(handler);
    }

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

    public JSONArray converContentHandlersToJson(Iterable<BigPipeContentHandler> handlers)
    {
        JSONArray result = new JSONArray();
        for (BigPipeContentHandler handler : handlers)
        {
            String html = handler.getFinalContent();
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
        return result;
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

    /**
     * Manages individual bit of content
     */
    private class InternalHandler implements BigPipeContentHandler
    {
        private volatile String content;

        private final String requestId;
        private final String contentId;
        private final RequestContentSet request;
        private final Function<String, String> successFunction;
        private final Function<Throwable, String> failureFunction;

        public InternalHandler(String requestId,
                               String contentId,
                               RequestContentSet request,
                               Function<String, String> successFunction,
                               Function<Throwable, String> failureFunction
        )
        {
            this.requestId = requestId;
            this.contentId = contentId;
            this.request = request;
            this.successFunction = successFunction;
            this.failureFunction = failureFunction;
        }

        public String getInitialContent()
        {
            if (content != null)
            {
                markCompleted();
                return successFunction.apply(content);
            }
            else
            {
                return "<span class=\"bp-" + contentId + " bp-loading\"></span>";
            }
        }

        @Override
        public void onSuccess(String content)
        {
            this.content = content;
            request.notifyConsumers();
        }

        @Override
        public void onFailure(Throwable ex)
        {
            this.content = failureFunction.apply(ex);
            request.notifyConsumers();
        }

        @Override
        public void markCompleted()
        {
            RequestContentSet request = requestContentSets.get(requestId);
            if (request != null)
            {
                request.removeContent(contentId);
            }
        }

        public String getFinalContent()
        {
            return successFunction.apply(content);
        }

        @Override
        public String getContentId()
        {
            return contentId;
        }

        public boolean isFinished()
        {
            return content != null;
        }
    }

    /**
     * Manages a set of content for a single page.  Content for the page can be waited upon in a
     * blocking call to be woken up when new content is available.
     */
    private class RequestContentSet
    {
        private final Map<String,InternalHandler> handlers = CopyOnWriteMap.newHashMap();

        /**
         * The expiration of the page content in the case where the xhr call to retrieve the
         * content is never made.
         */
        private final long expiry;
        private final String requestId;
        private final Object lock = new Object();

        public RequestContentSet(String requestId)
        {
            this.requestId = requestId;
            this.expiry = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(20);
        }

        /**
         * Ensures only one handler per contentid
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
            synchronized(lock)
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
         * Waits for content to be finished and removes it from the set of outstanding content if
         * done.  It will only wait until the timeout for the page has been reached.
         */
        public Iterable<BigPipeContentHandler> waitForFinishedContent()
        {
            List<BigPipeContentHandler> result = newArrayList();
            synchronized(lock)
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
