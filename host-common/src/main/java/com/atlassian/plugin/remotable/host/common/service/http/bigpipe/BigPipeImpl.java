package com.atlassian.plugin.remotable.host.common.service.http.bigpipe;

import com.atlassian.plugin.remotable.api.service.RequestContext;
import com.atlassian.plugin.remotable.api.service.http.bigpipe.BigPipe;
import com.atlassian.plugin.remotable.api.service.http.bigpipe.HtmlPromise;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.security.random.SecureRandomFactory;
import com.atlassian.util.concurrent.CopyOnWriteMap;
import com.atlassian.util.concurrent.ForwardingPromise;
import com.atlassian.util.concurrent.Promise;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.FutureCallback;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableSet;

/**
 * Manages big pipe instances
 */
public final class BigPipeImpl implements BigPipe
{
    private static final SecureRandom secureRandom = SecureRandomFactory.newInstance();
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final WebResourceManager webResourceManager;
    private final RequestIdAccessor requestIdAccessor = new RequestIdAccessor();
    private final UserIdRetriever userIdRetriever;

    ScheduledExecutorService cleanupThread = Executors.newSingleThreadScheduledExecutor();

    private final Map<String, RequestContentSet> requestContentSets = CopyOnWriteMap.newHashMap();

    private interface UserIdRetriever
    {
        public String getUserId();
    }

    BigPipeImpl(WebResourceManager webResourceManager, final RequestContext requestContext)
    {
        this(webResourceManager, new UserIdRetriever()
        {
            @Override
            public String getUserId()
            {
                return requestContext.getUserId();
            }
        });
    }

    @Autowired
    public BigPipeImpl(WebResourceManager webResourceManager, final UserManager userManager)
    {
        this(webResourceManager, new UserIdRetriever()
        {
            @Override
            public String getUserId()
            {
                return userManager.getRemoteUsername();
            }
        });
    }

    private BigPipeImpl(WebResourceManager webResourceManager, UserIdRetriever userIdRetriever)
    {
        this.webResourceManager = webResourceManager;
        this.userIdRetriever = userIdRetriever;
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
    public String getRequestId()
    {
        return requestIdAccessor.getRequestId();
    }

    public RequestIdAccessor getRequestIdAccessor()
    {
        return requestIdAccessor;
    }

    public HtmlPromise promiseHtmlContent(Promise<String> stringPromise)
    {
        // @todo figure out how to avoid this ugly ref cycle
        DefaultHtmlPromise htmlPromise = new DefaultHtmlPromise(stringPromise);
        final InternalHandler handler = registerContentPromise(BigPipe.HTML_CHANNEL_ID, htmlPromise);
        htmlPromise.setHandler(handler);
        return htmlPromise;
    }

    @Override
    public void promiseContent(String channelId, Promise<String> stringPromise)
    {
        registerContentPromise(channelId, stringPromise);
    }

    private InternalHandler registerContentPromise(String channelId, Promise<String> stringPromise)
    {
        ContentEnvelopePromise envelopePromise = new ContentEnvelopePromise(stringPromise, channelId);
        webResourceManager.requireResource("com.atlassian.labs.remoteapps-plugin:big-pipe");
        String requestId = getRequestId();
        RequestContentSet requestContentSet = requestContentSets.get(requestId);
        if (requestContentSet == null)
        {
            requestContentSet = new RequestContentSet(requestId, userIdRetriever.getUserId());
            requestContentSets.put(requestId, requestContentSet);
        }
        final RequestContentSet finalRequestContentSet = requestContentSet;
        envelopePromise.then(new FutureCallback<JSONObject>()
        {
            @Override
            public void onSuccess(JSONObject json)
            {
                finalRequestContentSet.notifyConsumers();
            }

            @Override
            public void onFailure(Throwable t)
            {
                finalRequestContentSet.notifyConsumers();
            }
        });
        InternalHandler handler = new InternalHandler(channelId, requestContentSet, envelopePromise);
        requestContentSet.addHandler(handler);
        return handler;
    }

    @Override
    public String consumeContent()
    {
        String requestId = getRequestId();
        RequestContentSet request = requestContentSets.get(requestId);
        Map<String, Collection<JSONObject>> finished;
        if (request != null)
        {
            String userId = userIdRetriever.getUserId();
            finished = request.consumeFinishedContent(userId);
        }
        else
        {
            finished = emptyMap();
        }
        return convertContentToJson(finished, requestId);
    }

    public String waitForContent(String requestId)
    {
        RequestContentSet request = requestContentSets.get(requestId);
        Map<String, Collection<JSONObject>> finished;
        if (request != null)
        {
            String userId = userIdRetriever.getUserId();
            finished = request.waitForFinishedContent(userId);
        }
        else
        {
            finished = emptyMap();
        }
        return convertContentToJson(finished, requestId);
    }

    @Override
    public boolean isActivated()
    {
        return requestContentSets.containsKey(getRequestId());
    }

    @SuppressWarnings("unchecked")
    private String convertContentToJson(Map<String, Collection<JSONObject>> contentByChannel, String requestId)
    {
        JSONObject response = new JSONObject();
        JSONArray items = new JSONArray();
        for (Collection<JSONObject> collection : contentByChannel.values())
        {
            for (JSONObject json : collection)
            {
                items.add(json);
            }
        }
        response.put("items", items);
        RequestContentSet requestContentSet = requestContentSets.get(requestId);
        JSONArray pendingChannelsArray = new JSONArray();
        Set<String> pendingChannels;
        if (requestContentSet != null)
        {
            pendingChannels = requestContentSet.getPendingChannelIds();
        }
        else
        {
            pendingChannels = newHashSet();
        }
        pendingChannelsArray.addAll(pendingChannels);
        response.put("pending", pendingChannelsArray);
        return response.toString();
    }

    /**
     * Manages individual bit of content
     */
    private class InternalHandler
    {
        private final String channelId;
        private final RequestContentSet request;
        private final Promise<JSONObject> jsonPromise;

        public InternalHandler(String channelId, RequestContentSet requestContentSet, Promise<JSONObject> jsonPromise)
        {
            this.channelId = channelId;
            this.request = requestContentSet;
            this.jsonPromise = jsonPromise.then(new FutureCallback<JSONObject>()
            {
                @Override
                public void onSuccess(JSONObject json)
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

        public String getChannelId()
        {
            return channelId;
        }

        public Promise<JSONObject> getContent()
        {
            return jsonPromise;
        }

        public boolean isFinished()
        {
            return jsonPromise.isDone();
        }

        public void removeContent()
        {
            request.removeContent(this);
        }

        @Override
        public boolean equals(Object obj)
        {
            return super.equals(obj);
        }

        @Override
        public int hashCode()
        {
            return super.hashCode();
        }
    }

    /**
     * Manages a set of content for a single page.  Content for the page can be waited upon in a blocking call to be
     * woken up when new content is available.
     */
    private class RequestContentSet
    {
        private final List<InternalHandler> handlers = new CopyOnWriteArrayList<InternalHandler>();

        /**
         * The expiration of the page content in the case where the xhr call to retrieve the content is never made.
         */
        private final long expiry;
        private final String requestId;
        private final String userId;
        private final Object lock = new Object();

        public RequestContentSet(String requestId, String userId)
        {
            this.requestId = requestId;
            this.userId = userId;
            this.expiry = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);
        }

        /**
         * Ensures only one handler per contentid
         *
         * @return The handler to actually use
         */
        public InternalHandler addHandler(InternalHandler handler)
        {
            handlers.add(handler);
            return handler;
        }

        public void removeContent(InternalHandler handler)
        {
            handlers.remove(handler);
        }

        public Map<String,Collection<JSONObject>> consumeFinishedContent(String userId)
        {
            verifyUser(userId);
            Multimap<String, JSONObject> result = ArrayListMultimap.create();
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
            return result.asMap();
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
        public Map<String,Collection<JSONObject>> waitForFinishedContent(String userId)
        {
            verifyUser(userId);
            Multimap<String, JSONObject> result = ArrayListMultimap.create();
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

            return result.asMap();
        }

        private void verifyUser(String userId)
        {
            if (userId == null ? this.userId != null : !userId.equals(this.userId))
            {
                throw new RuntimeException("Current user is not authorized to access requested bigpipe content");
            }
        }

        private void removeAllFinishedHandlers(Multimap<String, JSONObject> result)
        {
            for (InternalHandler handler : newHashSet(handlers))
            {
                if (handler.isFinished())
                {
                    removeContent(handler);
                    result.put(handler.getChannelId(), handler.getContent().claim());
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

        public Set<String> getPendingChannelIds()
        {
            Set<String> pendingChannelIds = newHashSet();
            for (InternalHandler handler : handlers)
            {
                pendingChannelIds.add(handler.getChannelId());
            }
            return unmodifiableSet(pendingChannelIds);
        }
    }

    private class DefaultHtmlPromise extends ForwardingPromise<String> implements HtmlPromise, MetadataProvider
    {
        private final Promise<String> delegate;
        private final String contentId;
        private InternalHandler handler;

        public DefaultHtmlPromise(Promise<String> delegate)
        {
            this.contentId = "bp-" + Long.toHexString(Math.abs(secureRandom.nextLong()));
            this.delegate = delegate;
        }

        public String getInitialContent()
        {
            if (delegate().isDone())
            {
                String content = delegate().claim();
                handler.removeContent();
                return "<span id=\"" + contentId + "\">" + content + "</span>";
            }
            else
            {
                return "<span id=\"" + contentId + "\" class=\"bp-loading\"></span>";
            }
        }

        @Override
        public Map<String, String> getMetadata()
        {
            return singletonMap("contentId", contentId);
        }

        @Override
        protected Promise<String> delegate()
        {
            return delegate;
        }

        public void setHandler(InternalHandler handler)
        {
            this.handler = handler;
        }
    }
}
