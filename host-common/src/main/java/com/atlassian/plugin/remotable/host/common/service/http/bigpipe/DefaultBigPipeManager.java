package com.atlassian.plugin.remotable.host.common.service.http.bigpipe;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.remotable.api.service.RequestContext;
import com.atlassian.plugin.remotable.api.service.http.bigpipe.*;
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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.*;

/**
 * Manages big pipe instances
 */
public final class DefaultBigPipeManager implements BigPipeManager, DisposableBean
{
    private static final SecureRandom secureRandom = SecureRandomFactory.newInstance();
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final WebResourceManager webResourceManager;
    private final RequestIdAccessor requestIdAccessor = new RequestIdAccessor();
    private final UserIdRetriever userIdRetriever;

    ScheduledExecutorService cleanupThread = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("Big Pipe Cleanup");
            return t;
        }
    });

    private final ConcurrentMap<String, BigPipeImpl> bigPipeImpls = CopyOnWriteMap.newHashMap();

    private interface UserIdRetriever
    {
        public String getUserId();
    }

    DefaultBigPipeManager(WebResourceManager webResourceManager, final RequestContext requestContext)
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
    public DefaultBigPipeManager(WebResourceManager webResourceManager, final UserManager userManager)
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

    private DefaultBigPipeManager(WebResourceManager webResourceManager, UserIdRetriever userIdRetriever)
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
        for (BigPipeImpl contentSet : bigPipeImpls.values())
        {
            if (contentSet.isExpired())
            {
                contentSet.removeBigPipeImpl();
            }
        }
    }

    public RequestIdAccessor getRequestIdAccessor()
    {
        return requestIdAccessor;
    }

    @Override
    public void destroy() throws Exception
    {
        cleanupThread.shutdownNow();
    }

    @Override
    public BigPipe getBigPipe()
    {
        return getBigPipe(getRequestId(), true);
    }

    @Override
    public Option<ConsumableBigPipe> getConsumableBigPipe()
    {
        return getConsumableBigPipe(getRequestId());
    }

    Option<ConsumableBigPipe> getConsumableBigPipe(String requestId)
    {
        ConsumableBigPipe result = null;
        if (requestId != null)
        {
            BigPipeImpl bigPipeImpl = bigPipeImpls.get(requestId);
            if (bigPipeImpl != null)
            {
                result = bigPipeImpl.getPendingChannelIds().size() > 0 ? bigPipeImpl : null;
            }
        }
        return Option.option(result);
    }

    private String getRequestId()
    {
        String requestId = requestIdAccessor.getRequestId();
        if (requestId == null)
        {
            throw new IllegalStateException("Current thread does not have a request id");
        }
        return requestId;
    }

    private BigPipeImpl getBigPipe(String requestId, boolean createIfAbsent)
    {
        checkNotNull(requestId);
        BigPipeImpl bigPipeImpl = bigPipeImpls.get(requestId);
        if (bigPipeImpl == null)
        {
            checkArgument(createIfAbsent, "No bigpipe instance found for request id: '%s'", requestId);
            BigPipeImpl newBigPipeImpl = new BigPipeImpl(requestId, userIdRetriever.getUserId());
            bigPipeImpl = bigPipeImpls.putIfAbsent(requestId, newBigPipeImpl);
            if (bigPipeImpl == null)
            {
                bigPipeImpl = newBigPipeImpl;
            }
        }
        return bigPipeImpl;
    }

    /**
     * Manages individual bit of content
     */
    private class InternalHandler
    {
        private final String channelId;
        private final BigPipeImpl bigPipe;
        private final Promise<JSONObject> jsonPromise;

        public InternalHandler(String channelId, BigPipeImpl bigPipeImpl, Promise<JSONObject> jsonPromise)
        {
            this.channelId = channelId;
            this.bigPipe = bigPipeImpl;
            this.jsonPromise = jsonPromise.then(new FutureCallback<JSONObject>()
            {
                @Override
                public void onSuccess(JSONObject json)
                {
                    bigPipe.notifyConsumers();
                }

                @Override
                public void onFailure(Throwable t)
                {
                    bigPipe.notifyConsumers();
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
            bigPipe.removeContent(this);
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
    private final class BigPipeImpl implements BigPipe, ConsumableBigPipe
    {
        private final List<InternalHandler> handlers;
        private final HtmlChannelImpl htmlChannel;
        private final ConcurrentMap<String, DataChannelImpl> dataChannels;

        /**
         * The expiration of the page content in the case where the xhr call to retrieve the content is never made.
         */
        private final long expiry;
        private final String requestId;
        private final String userId;
        private final Object lock = new Object();

        BigPipeImpl(String requestId, String userId)
        {
            this.requestId = requestId;
            this.userId = userId;
            this.handlers = new CopyOnWriteArrayList<InternalHandler>();
            this.htmlChannel = new HtmlChannelImpl();
            this.dataChannels = CopyOnWriteMap.newHashMap();
            this.expiry = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);
        }

        @Override
        public String getRequestId()
        {
            return requestId;
        }

        @Override
        public HtmlChannel getHtmlChannel()
        {
            return htmlChannel;
        }

        @Override
        public DataChannel getDataChannel(String channelId)
        {
            checkNotNull(channelId);
            checkArgument(!HTML_CHANNEL_ID.equals(channelId),
                "Data channels must not use the reserved channel id '%s'", HTML_CHANNEL_ID);

            DataChannel channel = dataChannels.get(channelId);
            if (channel == null)
            {
                DataChannelImpl newChannel = new DataChannelImpl(channelId);
                channel = dataChannels.putIfAbsent(channelId, newChannel);
                if (channel == null)
                {
                    channel = newChannel;
                }
            }
            return channel;
        }

        @Override
        public String consumeContent()
        {
            verifyUser(userId);
            Multimap<String, JSONObject> content = ArrayListMultimap.create();
            Set<String> pendingChannelIds;
            synchronized (lock)
            {
                if (hasMoreContent())
                {
                    removeAllFinishedHandlers(content);
                    if (!hasMoreContent())
                    {
                        // we've expired
                        log.info("All content has been consumed for request id {}", requestId);
                        removeBigPipeImpl();
                    }
                }
                pendingChannelIds = getPendingChannelIds();
            }
            return convertContentToJson(content.asMap(), pendingChannelIds);
        }

        @Override
        public String waitForContent()
        {
            verifyUser(userId);
            Multimap<String, JSONObject> content = ArrayListMultimap.create();
            Set<String> pendingChannelIds;
            synchronized (lock)
            {
                pendingChannelIds = getPendingChannelIds();
                if (hasMoreContent() || !pendingChannelIds.isEmpty())
                {
                    removeAllFinishedHandlers(content);
                    if (content.isEmpty() && !isExpired())
                    {
                        try
                        {
                            long timeout = expiry - System.currentTimeMillis();
                            if (timeout > 0)
                            {
                                lock.wait(timeout);
                            }
                            removeAllFinishedHandlers(content);
                        }
                        catch (InterruptedException e)
                        {
                            // ignore
                        }
                    }

                    if (isExpired())
                    {
                        // we've expired
                        log.info("Timeout waiting for {} jobs for request id {}", handlers.size(), requestId);
                        removeBigPipeImpl();
                    }

                    pendingChannelIds = getPendingChannelIds();
                }
            }
            return convertContentToJson(content.asMap(), pendingChannelIds);
        }

        @SuppressWarnings("unchecked")
        private String convertContentToJson(Map<String, Collection<JSONObject>> contentByChannel, Set<String> pendingChannelIds)
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
            JSONArray pendingChannelsArray = new JSONArray();
            pendingChannelsArray.addAll(pendingChannelIds);
            response.put("pending", pendingChannelsArray);
            return response.toString();
        }

        /**
         * Ensures only one handler per contentid
         *
         * @return The handler to actually use
         */
        private InternalHandler addHandler(InternalHandler handler)
        {
            handlers.add(handler);
            return handler;
        }

        public void removeContent(InternalHandler handler)
        {
            handlers.remove(handler);
        }

        private void removeBigPipeImpl()
        {
            handlers.clear();
            bigPipeImpls.remove(requestId);
        }

        private boolean hasMoreContent()
        {
            return !handlers.isEmpty();
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

        private boolean isExpired()
        {
            return System.currentTimeMillis() >= expiry;
        }

        private void notifyConsumers()
        {
            synchronized (lock)
            {
                lock.notifyAll();
            }
        }

        private Set<String> getPendingChannelIds()
        {
            Set<String> pendingChannelIds = newHashSet();
            synchronized (lock)
            {
                for (InternalHandler handler : handlers)
                {
                    pendingChannelIds.add(handler.getChannelId());
                }
                if (htmlChannel.isRetained())
                {
                    pendingChannelIds.add(htmlChannel.getId());
                }
                for (DataChannelImpl dataChannel : dataChannels.values())
                {
                    if (dataChannel.isRetained())
                    {
                        pendingChannelIds.add(dataChannel.getId());
                    }
                }
            }
            return unmodifiableSet(pendingChannelIds);
        }

        private InternalHandler registerContentPromise(String channelId, Promise<String> stringPromise)
        {
            ContentEnvelopePromise envelopePromise = new ContentEnvelopePromise(stringPromise, channelId);
            webResourceManager.requireResource("com.atlassian.labs.remoteapps-plugin:big-pipe");
            envelopePromise.then(new FutureCallback<JSONObject>()
            {
                @Override
                public void onSuccess(JSONObject json)
                {
                    notifyConsumers();
                }

                @Override
                public void onFailure(Throwable t)
                {
                    notifyConsumers();
                }
            });
            InternalHandler handler = new InternalHandler(channelId, this, envelopePromise);
            addHandler(handler);
            return handler;
        }

        private class HtmlChannelImpl extends AbstractChannel implements HtmlChannel
        {
            HtmlChannelImpl()
            {
                super(HTML_CHANNEL_ID);
            }

            @Override
            public String promiseContent(Promise<String> promise)
            {
                retainWhile(promise);
                HtmlPromise htmlPromise = new HtmlPromise(promise);
                final InternalHandler handler = registerContentPromise(BigPipe.HTML_CHANNEL_ID, htmlPromise);
                htmlPromise.setHandler(handler);
                return htmlPromise.getInitialContent();
            }
        }

        private class DataChannelImpl extends AbstractChannel implements DataChannel
        {
            DataChannelImpl(String id)
            {
                super(id);
            }

            @Override
            public void promiseContent(Promise<String> promise)
            {
                retainWhile(promise);
                registerContentPromise(getId(), promise);
            }
        }
    }

    private class HtmlPromise extends ForwardingPromise<String> implements MetadataProvider
    {
        private final Promise<String> delegate;
        private final String contentId;
        private InternalHandler handler;

        public HtmlPromise(Promise<String> delegate)
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
                return content;
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
