package com.atlassian.labs.remoteapps.plugin.webhook;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.api.service.http.HttpClient;
import com.atlassian.labs.remoteapps.plugin.webhook.event.WebHookPublishQueueFullEvent;
import com.atlassian.labs.remoteapps.spi.webhook.EventMatcher;
import com.atlassian.labs.remoteapps.spi.webhook.EventSerializer;
import com.atlassian.labs.remoteapps.spi.webhook.PluginUriResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.uri.Uri;
import com.atlassian.uri.UriBuilder;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.*;

/**
 * Publishes events to registered remote apps
 */
public final class WebHookPublisherImpl implements DisposableBean, WebHookPublisher
{
    private static final Logger log = LoggerFactory.getLogger(WebHookPublisherImpl.class);

    public static final int PUBLISH_QUEUE_SIZE = 100;

    private final ThreadPoolExecutor publisher;
    private final HttpClient httpClient;
    private final EventPublisher eventPublisher;
    private final UserManager userManager;
    private final PluginUriResolver pluginUriResolver;

    private final Multimap<String, Registration> registrationsByEvent = newMultimap();

    @Autowired
    public WebHookPublisherImpl(HttpClient httpClient, EventPublisher eventPublisher, UserManager userManager, PluginUriResolver pluginUriResolver)
    {
        this.httpClient = checkNotNull(httpClient);
        this.eventPublisher = checkNotNull(eventPublisher);
        this.userManager = checkNotNull(userManager);
        this.pluginUriResolver = checkNotNull(pluginUriResolver);
        this.publisher = new ThreadPoolExecutor(3, 3, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(PUBLISH_QUEUE_SIZE));
    }

    @Override
    public void register(String pluginKey, String eventIdentifier, URI path)
    {
        registrationsByEvent.put(eventIdentifier, new Registration(pluginKey, path));
    }

    @Override
    public void unregister(String pluginKey, String eventIdentifier, URI url)
    {
        registrationsByEvent.remove(eventIdentifier, new Registration(pluginKey, url));
    }

    @Override
    public void publish(String eventIdentifier, EventMatcher<Object> eventMatcher, EventSerializer eventSerializer)
    {
        String body = null;
        for (Registration registration : registrationsByEvent.get(eventIdentifier))
        {
            if (eventMatcher.matches(eventSerializer.getEvent(), registration.getPluginKey()))
            {
                body = body != null ? body : eventSerializer.getJson();
                final String username = userManager.getRemoteUsername();
                final PublishTask task = new PublishTask(httpClient, registration, username != null ? username : "", body, pluginUriResolver);
                try
                {
                    publisher.execute(task);
                }
                catch (RejectedExecutionException ex)
                {
                    log.warn("Web hook queue full, rejecting '{}'", task);
                    eventPublisher.publish(new WebHookPublishQueueFullEvent(eventIdentifier, null));
                }
            }
            else
            {
                log.debug("Matcher {} didn't match plugin key {}", eventMatcher, registration.getPluginKey());
            }
        }
    }

    private Multimap<String, Registration> newMultimap()
    {
        return Multimaps.synchronizedMultimap(
                Multimaps.newMultimap(Maps.<String, Collection<Registration>>newHashMap(),
                        new Supplier<Collection<Registration>>()
                        {
                            public Collection<Registration> get()
                            {
                                return Sets.newHashSet();
                            }
                        }));
    }

    @Override
    public void destroy() throws Exception
    {
        publisher.shutdownNow();
    }

    private static class PublishTask implements Runnable
    {
        private final Registration registration;
        private final String userName;
        private final String body;
        private final HttpClient httpClient;
        private final PluginUriResolver pluginUriResolver;

        public PublishTask(HttpClient httpClient, Registration registration, String userName, String body, PluginUriResolver pluginUriResolver)
        {
            this.httpClient = checkNotNull(httpClient);
            this.registration = checkNotNull(registration);
            this.userName = checkNotNull(userName);
            this.body = checkNotNull(body);
            this.pluginUriResolver = checkNotNull(pluginUriResolver);
        }

        @Override
        public void run()
        {
            final URI url = new UriBuilder(Uri.fromJavaUri(pluginUriResolver.getUri(registration.getPluginKey(), registration.getPath())))
                    .addQueryParameter("user_id", userName)
                    .toUri().toJavaUri();
            log.debug("Posting to web hook at " + url + "\n" + body);

            // our job is just to send this, not worry about whether it failed or not
            httpClient
                    .newRequest(url, "application/json", body)
//                  .setHeader("Authorization", authorization)
                    // attributes capture optional properties sent to analytics
                    .setAttribute("purpose", "web-hook-notification")
                    .setAttribute("pluginKey", registration.getPluginKey())
                    .post();
        }

        @Override
        public String toString()
        {
            return "PublishTask{" +
                    "registration=" + registration +
                    ", body='" + body + '\'' +
                    '}';
        }
    }

    private static class Registration
    {
        private final String pluginKey;
        private final URI path;

        public Registration(String pluginKey, URI path)
        {
            this.pluginKey = checkNotNull(pluginKey);
            this.path = checkNotNull(path);
        }

        public String getPluginKey()
        {
            return pluginKey;
        }

        public URI getPath()
        {
            return path;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            Registration that = (Registration) o;

            if (!pluginKey.equals(that.pluginKey))
            {
                return false;
            }
            if (!path.equals(that.path))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = pluginKey.hashCode();
            result = 31 * result + path.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return "Registration{" +
                    "pluginKey=" + pluginKey +
                    ", path='" + path + '\'' +
                    '}';
        }
    }
}
