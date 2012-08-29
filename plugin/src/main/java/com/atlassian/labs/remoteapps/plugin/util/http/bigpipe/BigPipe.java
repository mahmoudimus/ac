package com.atlassian.labs.remoteapps.plugin.util.http.bigpipe;

import com.atlassian.labs.remoteapps.plugin.ContentRetrievalException;
import com.atlassian.security.random.SecureRandomFactory;
import com.atlassian.util.concurrent.CopyOnWriteMap;
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
public class BigPipe
{
    private final SecureRandom secureRandom = SecureRandomFactory.newInstance();
    private static final Logger log = LoggerFactory.getLogger(BigPipe.class);

    private final Map<String,RequestContentSet> requestContentSets = CopyOnWriteMap.newHashMap();

    public BigPipeHttpContentHandler createContentHandler(String requestId, ContentProcessor contentProcessor)
    {
        // give each content its own unique id for replacing later
        String contentId = String.valueOf(secureRandom.nextLong());

        RequestContentSet request = requestContentSets.get(requestId);
        if (request == null)
        {
            request = new RequestContentSet(requestId);
            requestContentSets.put(requestId, request);
        }

        InternalHandler handler = new InternalHandler(requestId, contentId,
                request, contentProcessor);
        request.addHandler(handler);
        return handler;
    }

    public Iterable<BigPipeHttpContentHandler> waitForCompletedHandlers(String requestId)
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

    public JSONArray converContentHandlersToJson(Iterable<BigPipeHttpContentHandler> handlers)
    {
        JSONArray result = new JSONArray();
        for (BigPipeHttpContentHandler handler : handlers)
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

    public Iterable<BigPipeHttpContentHandler> consumeCompletedHandlers(String requestId)
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
    private class InternalHandler implements BigPipeHttpContentHandler
    {
        private volatile String content;

        private final String requestId;
        private final String contentId;
        private final RequestContentSet request;
        private final ContentProcessor contentProcessor;

        public InternalHandler(String requestId, String contentId,
                RequestContentSet request, ContentProcessor contentProcessor)
        {
            this.requestId = requestId;
            this.contentId = contentId;
            this.request = request;
            this.contentProcessor = contentProcessor;
        }

        public String getInitialContent()
        {
            if (content != null)
            {
                RequestContentSet request = requestContentSets.get(requestId);
                if (request != null)
                {
                    request.removeContent(contentId);
                }
                return contentProcessor.process(content);
            }
            else
            {
                return "<span id=\"" + contentId + "\" class=\"bp-loading\"></span>";
            }
        }

        @Override
        public void onSuccess(String content)
        {
            this.content = content;
            request.notifyConsumers();
        }

        @Override
        public void onError(ContentRetrievalException ex)
        {
            this.content = "<div class=\"bp-error\">Error: " + ex.getMessage() + "</div>";
            request.notifyConsumers();
        }

        public String getFinalContent()
        {
            return contentProcessor.process(content);
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

        public void addHandler(InternalHandler handler)
        {
            handlers.put(handler.contentId, handler);
        }

        public void removeContent(String contentId)
        {
            handlers.remove(contentId);
        }

        public Iterable<BigPipeHttpContentHandler> consumeFinishedContent()
        {
            List<BigPipeHttpContentHandler> result = newArrayList();
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
        public Iterable<BigPipeHttpContentHandler> waitForFinishedContent()
        {
            List<BigPipeHttpContentHandler> result = newArrayList();
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

        private void removeAllFinishedHandlers(List<BigPipeHttpContentHandler> result)
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
