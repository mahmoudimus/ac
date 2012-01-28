package com.atlassian.labs.remoteapps.webhook;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.util.http.CachingHttpContentRetriever;
import com.atlassian.labs.remoteapps.event.RemoteAppEvent;
import com.atlassian.labs.remoteapps.util.http.HttpContentRetriever;
import com.atlassian.labs.remoteapps.webhook.event.WebHookPublishQueueFullEvent;
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
import java.util.concurrent.*;

/**
 * Publishes events to registered remote apps
 */
@Component
public class WebHookPublisher implements DisposableBean
{
    public static final int PUBLISH_QUEUE_SIZE = 100;
    private final ThreadPoolExecutor publisher;
    private final HttpContentRetriever httpContentRetriever;
    private final ApplicationLinkService applicationLinkService;
    private final EventPublisher eventPublisher;
    private static final Logger log = LoggerFactory.getLogger(WebHookPublisher.class);

    private final Multimap<String, Registration> registrationsByEvent = newMultimap();

    @Autowired
    public WebHookPublisher(HttpContentRetriever httpContentRetriever, ApplicationLinkService applicationLinkService,
                            EventPublisher eventPublisher)
    {
        this.httpContentRetriever = httpContentRetriever;
        this.applicationLinkService = applicationLinkService;
        this.eventPublisher = eventPublisher;

        publisher = new ThreadPoolExecutor(3, 3,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>(PUBLISH_QUEUE_SIZE));
    }

    public void register(NonAppLinksApplicationType applicationType, String eventIdentifier, String path)
    {
        registrationsByEvent.put(eventIdentifier, new Registration(applicationType, path));
    }

    public void unregister(NonAppLinksApplicationType applicationType, String eventIdentifier, String url)
    {
        registrationsByEvent.remove(eventIdentifier, new Registration(applicationType, url));
    }

    public void publish(String eventIdentifier, EventSerializer eventSerializer)
    {
        String body = null;
        for (Registration registration : registrationsByEvent.get(eventIdentifier))
        {
            if (registration.applies(eventSerializer.getEvent()))
            {
                ApplicationLink link = applicationLinkService.getPrimaryApplicationLink(registration.getApplicationType().getClass());
                if (link != null)
                {
                    body = body != null ? body : eventSerializer.getJson();
                    PublishTask task = new PublishTask(httpContentRetriever, registration, link, body);
                    try
                    {
                        publisher.execute(task);
                    }
                    catch (RejectedExecutionException ex)
                    {
                        log.warn("Web hook queue full, rejecting '{}'", task);
                        eventPublisher.publish(new WebHookPublishQueueFullEvent(eventIdentifier, link));
                    }
                }
                else
                {
                    log.warn("Primary link for '" + registration.getApplicationType().getI18nKey() + "' not found");
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
        private final ApplicationLink applicationLink;
        private final String body;

        public PublishTask(HttpContentRetriever httpContentRetriever, Registration registration, ApplicationLink applicationLink,
                           String body)
        {
            this.httpContentRetriever = httpContentRetriever;
            this.registration = registration;
            this.applicationLink = applicationLink;
            this.body = body;
        }

        @Override
        public void run()
        {
            String url = registration.getUrl(applicationLink);
            log.debug("Posting to web hook at " + url + "\n" + body);
            httpContentRetriever.postIgnoreResponse(applicationLink, url, body);
        }

        @Override
        public String toString()
        {
            return "PublishTask{" +
                    "registration=" + registration +
                    ", applicationLink=" + applicationLink +
                    ", body='" + body + '\'' +
                    '}';
        }
    }

    private static class Registration
    {
        private final NonAppLinksApplicationType applicationType;
        private final String path;

        public Registration(NonAppLinksApplicationType applicationType, String path)
        {
            this.applicationType = applicationType;
            this.path = path;
        }

        public ApplicationType getApplicationType()
        {
            return applicationType;
        }

        public String getUrl(ApplicationLink link)
        {
            return link.getRpcUrl() + path;
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

            if (!applicationType.equals(that.applicationType))
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
            int result = applicationType.hashCode();
            result = 31 * result + path.hashCode();
            return result;
        }

        public boolean applies(Object event)
        {
            return !(event instanceof RemoteAppEvent) ||
                ((RemoteAppEvent)event).getRemoteAppKey().equals(applicationType.getId().get());
        }

        @Override
        public String toString()
        {
            return "Registration{" +
                    "applicationType=" + applicationType +
                    ", path='" + path + '\'' +
                    '}';
        }
    }


}
