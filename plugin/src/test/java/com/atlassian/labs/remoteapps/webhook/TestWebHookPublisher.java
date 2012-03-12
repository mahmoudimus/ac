package com.atlassian.labs.remoteapps.webhook;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.modules.applinks.RemoteAppApplicationType;
import com.atlassian.labs.remoteapps.util.http.HttpContentRetriever;
import com.atlassian.labs.remoteapps.webhook.event.WebHookPublishQueueFullEvent;
import com.atlassian.labs.remoteapps.webhook.external.EventMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.net.URI;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestWebHookPublisher
{
    @Mock
    HttpContentRetriever httpContentRetriever;

    @Mock
    ApplicationLinkService applicationLinkService;

    @Mock
    EventPublisher eventPublisher;

    @Mock ApplicationLink link;


    WebHookPublisher publisher;
    private ApplicationLinkDetails details;
    private RemoteAppApplicationType type;

    @Before
    public void setUp()
    {
        initMocks(this);
        publisher = new WebHookPublisher(httpContentRetriever, applicationLinkService, eventPublisher);
        details = ApplicationLinkDetails.builder()
                .displayUrl(URI.create("http://example.com/foo"))
                                    .name("Foo")
                                    .rpcUrl(URI.create("http://example.com/foo"))
                                    .isPrimary(true)
                                    .build();
        type = new RemoteAppApplicationType(new TypeId("foo"), "test", null, details);

        when(link.getRpcUrl()).thenReturn(details.getRpcUrl());
        when(applicationLinkService.getPrimaryApplicationLink(type.getClass())).thenReturn(link);
    }

    @After
    public void tearDown() throws Exception
    {
        publisher.destroy();
    }

    @Test
    public void testPublishSuccess()
    {
        publisher.register(type, "event.id", "/event");
        publisher.publish("event.id",  EventMatcher.ALWAYS_TRUE, new MapEventSerializer("event_object",
                                                              Collections.<String, Object>singletonMap("field", "value")));

    }

    @Test
    public void testPublishFailureDueToMatching()
    {
        publisher.register(type, "event.id", "/event");
        publisher.publish("event.id", new FalseEventMatcher(), new MapEventSerializer("event_object",
                Collections.<String, Object>singletonMap("field", "value")));
        verify(httpContentRetriever, never()).postIgnoreResponse(Matchers.<ApplicationLink>any(),
                anyString(),
                anyString());

    }

    @Test
    public void testPublishToNone()
    {
        publisher.publish("event.id", EventMatcher.ALWAYS_TRUE, new MapEventSerializer("event_object",
                                                              Collections.<String, Object>singletonMap("field", "value")));

        verify(httpContentRetriever, never()).postIgnoreResponse(Matchers.<ApplicationLink>any(),
                                                                 anyString(),
                                                                 anyString());
    }

    @Test
    public void testPublishToNoneWithRegistrations()
    {
        publisher.register(type, "event.other.id", "/event");
        publisher.publish("event.id", EventMatcher.ALWAYS_TRUE,  new MapEventSerializer("event_object",
                                                              Collections.<String, Object>singletonMap("field", "value")));

        verify(httpContentRetriever, never()).postIgnoreResponse(Matchers.<ApplicationLink>any(),
                                                                 anyString(),
                                                                 anyString());
    }

    @Test
    public void testPublishCallSuccessfulEvenIfSaturated()
    {
        publisher = new WebHookPublisher(new SleepingHttpContentRetriever(), applicationLinkService, eventPublisher);
        publisher.register(type, "event.id", "/event");

        for (int x=0; x<100 + 4; x++)
        {
            publisher.publish("event.id", EventMatcher.ALWAYS_TRUE, new MapEventSerializer("event_object",
                                                                  Collections.<String, Object>singletonMap("field", "value")));
        }

        verify(eventPublisher, times(1)).publish(argThat(new ArgumentMatcher<Object>()
        {
            @Override
            public boolean matches(Object argument)
            {
                WebHookPublishQueueFullEvent event = (WebHookPublishQueueFullEvent) argument;
                return event.getApplicationLink() == link && "event.id".equals(event.getEventIdentifier());
            }
        }));
    }

    private static class FalseEventMatcher implements EventMatcher<Object>
    {
        @Override
        public boolean matches(Object event, ApplicationLink appLink)
        {
            return false;
        }
    }
}
