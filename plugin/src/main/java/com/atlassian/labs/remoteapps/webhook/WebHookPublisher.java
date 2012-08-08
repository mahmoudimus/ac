package com.atlassian.labs.remoteapps.webhook;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.RemoteAppAccessor;
import com.atlassian.labs.remoteapps.RemoteAppAccessorFactory;
import com.atlassian.labs.remoteapps.event.RemoteAppEvent;
import com.atlassian.labs.remoteapps.util.http.HttpContentRetriever;
import com.atlassian.labs.remoteapps.util.uri.Uri;
import com.atlassian.labs.remoteapps.util.uri.UriBuilder;
import com.atlassian.labs.remoteapps.webhook.event.WebHookPublishQueueFullEvent;
import com.atlassian.labs.remoteapps.webhook.external.EventMatcher;
import com.atlassian.labs.remoteapps.webhook.external.EventSerializer;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Publishes events to registered remote apps
 */
@Component
public class WebHookPublisher implements DisposableBean
{
    public static final int PUBLISH_QUEUE_SIZE = 100;
    private final ThreadPoolExecutor publisher;
    private final HttpContentRetriever httpContentRetriever;
    private final EventPublisher eventPublisher;
    private final UserManager userManager;
    private final RemoteAppAccessorFactory remoteAppAccessorFactory;
    private static final Logger log = LoggerFactory.getLogger(WebHookPublisher.class);

    private final Multimap<String, Registration> registrationsByEvent = newMultimap();

    @Autowired
    public WebHookPublisher(HttpContentRetriever httpContentRetriever,
            EventPublisher eventPublisher, UserManager userManager,
            RemoteAppAccessorFactory remoteAppAccessorFactory)
    {
        this.httpContentRetriever = httpContentRetriever;
        this.eventPublisher = eventPublisher;
        this.userManager = userManager;
        this.remoteAppAccessorFactory = remoteAppAccessorFactory;

        publisher = new ThreadPoolExecutor(3, 3,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>(PUBLISH_QUEUE_SIZE));
    }

    public void register(String pluginKey, String eventIdentifier, String path)
    {
        registrationsByEvent.put(eventIdentifier, new Registration(pluginKey, path));
    }

    public void unregister(String pluginKey, String eventIdentifier, String url)
    {
        registrationsByEvent.remove(eventIdentifier, new Registration(pluginKey, url));
    }

    public void publish(String eventIdentifier, EventMatcher<Object> eventMatcher, EventSerializer eventSerializer)
    {
        String body = null;
        for (Registration registration : registrationsByEvent.get(eventIdentifier))
        {
            if (registration.applies(eventSerializer.getEvent()))
            {

                if (eventMatcher.matches(eventSerializer.getEvent(), registration.getPluginKey()))
                {
                    RemoteAppAccessor remoteAppAccessor = remoteAppAccessorFactory.get(
                            registration.getPluginKey());
                    body = body != null ? body : eventSerializer.getJson();
                    String username = userManager.getRemoteUsername();
                    PublishTask task = new PublishTask(httpContentRetriever, registration,
                            remoteAppAccessor, username != null ? username : "", body);
                    try
                    {
                        publisher.execute(task);
                    }
                    catch (RejectedExecutionException ex)
                    {
                        log.warn("Web hook queue full, rejecting '{}'", task);
                        eventPublisher.publish(new WebHookPublishQueueFullEvent(eventIdentifier,
                                remoteAppAccessor.getKey()));
                    }
                }
                else
                {
                    log.debug("Matcher {} didn't match plugin key {}", eventMatcher, registration.getPluginKey());
                }
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
        private final HttpContentRetriever httpContentRetriever;
        private final Registration registration;
        private final RemoteAppAccessor remoteAppAccessor;
        private final String userName;
        private final String body;

        public PublishTask(HttpContentRetriever httpContentRetriever,
                Registration registration, RemoteAppAccessor remoteAppAccessor, String userName,
                           String body)
        {
            this.httpContentRetriever = httpContentRetriever;
            this.registration = registration;
            this.remoteAppAccessor = remoteAppAccessor;
            this.userName = userName;
            this.body = body;
        }

        @Override
        public void run()
        {
            String url = new UriBuilder(Uri.parse(registration.getUrl(remoteAppAccessor)))
                    .addQueryParameter("user_id", userName).toString();

            log.debug("Posting to web hook at " + url + "\n" + body);
            httpContentRetriever.postIgnoreResponse(remoteAppAccessor.getAuthorizationGenerator(), url, body);
        }

        @Override
        public String toString()
        {
            return "PublishTask{" +
                    "registration=" + registration +
                    ", appKey=" + remoteAppAccessor.getKey() +
                    ", body='" + body + '\'' +
                    '}';
        }
    }

    private static class Registration
    {
        private final String pluginKey;
        private final String path;

        public Registration(String pluginKey, String path)
        {
            this.pluginKey = pluginKey;
            this.path = path;
        }

        public String getPluginKey()
        {
            return pluginKey;
        }

        public String getUrl(RemoteAppAccessor remoteAppAccessor)
        {
            return remoteAppAccessor.getDisplayUrl() + path;
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

        public boolean applies(Object event)
        {
            return !(event instanceof RemoteAppEvent) ||
                ((RemoteAppEvent)event).getRemoteAppKey().equals(pluginKey);
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
