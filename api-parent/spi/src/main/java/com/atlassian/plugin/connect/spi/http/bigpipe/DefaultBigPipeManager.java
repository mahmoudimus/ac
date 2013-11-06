package com.atlassian.plugin.connect.spi.http.bigpipe;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.api.service.http.bigpipe.*;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.security.random.SecureRandomFactory;
import com.atlassian.util.concurrent.CopyOnWriteMap;
import com.atlassian.util.concurrent.ForwardingPromise;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Supplier;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.FutureCallback;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import static com.atlassian.fugue.Option.none;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableSet;

/**
 * Manages big pipe instances
 */
@Component
public final class DefaultBigPipeManager implements BigPipeManager, DisposableBean
{
    private static final SecureRandom secureRandom = SecureRandomFactory.newInstance();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RequestIdAccessor requestIdAccessor = new RequestIdAccessor();
    private final UserIdRetriever userIdRetriever;

    private final ScheduledExecutorService cleanupThread;

    private final ConcurrentMap<String, BigPipeImpl> bigPipeImpls = new LoggingConcurrentMap(logger, CopyOnWriteMap.<String, BigPipeImpl>newHashMap());

    private interface UserIdRetriever
    {
        public String getUserId();
    }

    // called by atlassian-connect-plugin for its own use
    @Autowired
    public DefaultBigPipeManager(final UserManager userManager)
    {
        this(new UserIdRetriever()
        {
            @Override
            public String getUserId()
            {
                UserKey userKey = userManager.getRemoteUserKey();
                return userKey == null ? null : userKey.getStringValue();
            }
        }, createCleanupThread());
    }

    private DefaultBigPipeManager(UserIdRetriever userIdRetriever, ScheduledExecutorService cleanupThread)
    {
        this.userIdRetriever = userIdRetriever;
        this.cleanupThread = cleanupThread;
        cleanupThread.scheduleAtFixedRate(new Runnable()
        {
            @Override
            public void run()
            {
                cleanExpiredRequests();
            }
        }, 2, 1, TimeUnit.MINUTES);
    }

    public static ScheduledExecutorService createCleanupThread()
    {
        return Executors.newSingleThreadScheduledExecutor(new ThreadFactory()
        {
            @Override
            public Thread newThread(Runnable r)
            {
                ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
                try
                {
                    Thread.currentThread().setContextClassLoader(null);
                    Thread t = new Thread(r);
                    t.setName("Big Pipe Cleanup");
                    return t;
                }
                finally
                {
                    Thread.currentThread().setContextClassLoader(oldCl);
                }
            }
        });
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
        if (requestId == null)
        {
            logger.debug("No consumable big pipe for null request id.");
            return none();
        }

        final BigPipeImpl bigPipeImpl = bigPipeImpls.get(requestId);
        if (bigPipeImpl == null)
        {
            logger.debug("No consumable big pipe for request '{}'.", requestId);
            return none();
        }

        final Set<String> pendingChannelIds = bigPipeImpl.getPendingChannelIds();
        if (pendingChannelIds.isEmpty())
        {
            logger.debug("Found big pipe for request '{}', but no pending channels", requestId);
            return none();
        }
        else
        {
            logger.debug("Found big pipe for request '{}', with pending channel(s): {}", requestId, pendingChannelIds);
            return Option.<ConsumableBigPipe>some(bigPipeImpl);
        }
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
    private static final class InternalHandler
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
        public String toString()
        {
            return "Internal handler for channel '" + channelId + "' and " + bigPipe;
        }
    }

    /**
     * Manages a set of content for a single page.  Content for the page can be waited upon in a blocking call to be
     * woken up when new content is available.
     */
    final class BigPipeImpl implements BigPipe, ConsumableBigPipe
    {
        private final List<InternalHandler> handlers;
        private final HtmlChannelImpl htmlChannel;
        private final HtmlChannelImpl scriptChannel;
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
            this.handlers = new LoggingList<InternalHandler>(logger, format("request '%s' handlers'", requestId), new CopyOnWriteArrayList<InternalHandler>());
            this.htmlChannel = new HtmlContentChannelImpl();
            this.scriptChannel = new ScriptContentChannelImpl();
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
        public HtmlChannel getScriptChannel()
        {
            return scriptChannel;
        }

        @Override
        public DataChannel getDataChannel(String channelId)
        {
            checkNotNull(channelId);
            checkArgument(!HTML_CHANNEL_ID.equals(channelId),
                    "Data channels must not use the reserved channel id '%s'", HTML_CHANNEL_ID);
            checkArgument(!SCRIPT_CHANNEL_ID.equals(channelId),
                    "Data channels must not use the reserved channel id '%s'", SCRIPT_CHANNEL_ID);

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
                        logger.info("All content has been consumed for request id {}", requestId);
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
                        logger.info("Timeout waiting for {} jobs for request id {}", handlers.size(), requestId);
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
                if (scriptChannel.isRetained())
                {
                    pendingChannelIds.add(scriptChannel.getId());
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

        private class HtmlContentChannelImpl extends HtmlChannelImpl
        {
            private HtmlContentChannelImpl() {
                super(HTML_CHANNEL_ID);
            }
        }

        private class ScriptContentChannelImpl extends HtmlChannelImpl
        {
            private ScriptContentChannelImpl() {
                super(SCRIPT_CHANNEL_ID);
            }
        }

        private abstract class HtmlChannelImpl extends AbstractChannel implements HtmlChannel
        {
            private final String channelId;

            HtmlChannelImpl(String channelId)
            {
                super(channelId);
                this.channelId = channelId;
            }

            @Override
            public Supplier<String> promiseContent(Promise<String> promise)
            {
                retainWhile(promise);
                final HtmlPromise htmlPromise = new HtmlPromise(promise);

                final InternalHandler handler = registerContentPromise(channelId, htmlPromise);
                logger.debug("Added big pipe content with id {} to request {}", htmlPromise.contentId, requestId);

                htmlPromise.setHandler(handler);
                return new Supplier<String>()
                {
                    @Override
                    public String get()
                    {
                        return htmlPromise.getInitialContent();
                    }
                };
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

        @Override
        public String toString()
        {
            return "BigPipe for request '" + requestId + "' and user '" + userId + "'";
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
