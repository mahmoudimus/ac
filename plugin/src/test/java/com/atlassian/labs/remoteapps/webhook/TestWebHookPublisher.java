package com.atlassian.labs.remoteapps.webhook;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.RemoteAppAccessor;
import com.atlassian.labs.remoteapps.util.http.AuthorizationGenerator;
import com.atlassian.labs.remoteapps.util.http.HttpContentRetriever;
import com.atlassian.labs.remoteapps.webhook.event.WebHookPublishQueueFullEvent;
import com.atlassian.labs.remoteapps.webhook.external.EventMatcher;
import com.atlassian.sal.api.user.UserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestWebHookPublisher
{
    @Mock
    HttpContentRetriever httpContentRetriever;

    @Mock
    EventPublisher eventPublisher;

    WebHookPublisher publisher;
    private RemoteAppAccessor remoteAppAccessor;

    @Before
    public void setUp()
    {
        initMocks(this);
        publisher = new WebHookPublisher(httpContentRetriever, eventPublisher,
                mock(UserManager.class));
        remoteAppAccessor = mock(RemoteAppAccessor.class);
        when(remoteAppAccessor.getKey()).thenReturn("foo");
    }

    @After
    public void tearDown() throws Exception
    {
        publisher.destroy();
    }

    @Test
    public void testPublishSuccess()
    {
        publisher.register(remoteAppAccessor, "event.id", "/event");
        publisher.publish("event.id",  EventMatcher.ALWAYS_TRUE, new MapEventSerializer("event_object",
                                                              Collections.<String, Object>singletonMap("field", "value")));

    }

    @Test
    public void testPublishFailureDueToMatching()
    {
        publisher.register(remoteAppAccessor, "event.id", "/event");
        publisher.publish("event.id", new FalseEventMatcher(), new MapEventSerializer("event_object",
                Collections.<String, Object>singletonMap("field", "value")));
        verify(httpContentRetriever, never()).postIgnoreResponse(Matchers.<AuthorizationGenerator>any(),
                anyString(),
                anyString());

    }

    @Test
    public void testPublishToNone()
    {
        publisher.publish("event.id", EventMatcher.ALWAYS_TRUE, new MapEventSerializer("event_object",
                                                              Collections.<String, Object>singletonMap("field", "value")));

        verify(httpContentRetriever, never()).postIgnoreResponse(Matchers.<AuthorizationGenerator>any(),
                                                                 anyString(),
                                                                 anyString());
    }

    @Test
    public void testPublishToNoneWithRegistrations()
    {
        publisher.register(remoteAppAccessor, "event.other.id", "/event");
        publisher.publish("event.id", EventMatcher.ALWAYS_TRUE,  new MapEventSerializer("event_object",
                                                              Collections.<String, Object>singletonMap("field", "value")));

        verify(httpContentRetriever, never()).postIgnoreResponse(Matchers.<AuthorizationGenerator>any(),
                                                                 anyString(),
                                                                 anyString());
    }

    @Test
    public void testPublishCallSuccessfulEvenIfSaturated()
    {
        publisher = new WebHookPublisher(new SleepingHttpContentRetriever(), eventPublisher,
                mock(UserManager.class));
        publisher.register(remoteAppAccessor, "event.id", "/event");

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
                return event.getAppKey().equals(remoteAppAccessor.getKey()) &&
                        "event.id".equals(event.getEventIdentifier());
            }
        }));
    }

    private static class FalseEventMatcher implements EventMatcher<Object>
    {
        @Override
        public boolean matches(Object event, String pluginKey)
        {
            return false;
        }
    }
}
