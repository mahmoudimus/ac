package com.atlassian.labs.remoteapps.plugin.webhook;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.labs.remoteapps.host.common.service.http.DefaultHttpClient;
import com.atlassian.labs.remoteapps.plugin.webhook.event.WebHookPublishQueueFullEvent;
import com.atlassian.labs.remoteapps.spi.webhook.EventMatcher;
import com.atlassian.sal.api.user.UserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;

import java.net.URI;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

public final class TestWebHookPublisher
{
    @Mock
    private DefaultHttpClient httpClient;

    @Mock
    private EventPublisher eventPublisher;

    private WebHookPublisherImpl publisher;

    @Before
    public void setUp()
    {
        initMocks(this);
        publisher = new WebHookPublisherImpl(httpClient, eventPublisher, mock(UserManager.class));
    }

    @After
    public void tearDown() throws Exception
    {
        publisher.destroy();
    }

    @Test
    public void testPublishSuccess()
    {
        publisher.register("foo", "event.id", URI.create("/event"));
        publisher.publish("event.id", EventMatcher.ALWAYS_TRUE, new MapEventSerializer("event_object",
                Collections.<String, Object>singletonMap("field", "value")));
    }

    @Test
    public void testPublishFailureDueToMatching()
    {
        publisher.register("foo", "event.id", URI.create("/event"));
        publisher.publish("event.id", new FalseEventMatcher(), new MapEventSerializer("event_object",
                Collections.<String, Object>singletonMap("field", "value")));

        verify(httpClient, never()).newRequest(any(URI.class), anyString(), anyString());
    }

    @Test
    public void testPublishToNone()
    {
        publisher.publish("event.id", EventMatcher.ALWAYS_TRUE, new MapEventSerializer("event_object",
                Collections.<String, Object>singletonMap("field", "value")));

        verify(httpClient, never()).newRequest(any(URI.class), anyString(), anyString());
    }

    @Test
    public void testPublishToNoneWithRegistrations()
    {
        publisher.register("foo", "event.other.id", URI.create("/event"));
        publisher.publish("event.id", EventMatcher.ALWAYS_TRUE, new MapEventSerializer("event_object",
                Collections.<String, Object>singletonMap("field", "value")));

        verify(httpClient, never()).newRequest(any(URI.class), anyString(), anyString());
    }

    @Test
    public void testPublishCallSuccessfulEvenIfSaturated()
    {
        publisher = new WebHookPublisherImpl(new SleepingHttpClient(), eventPublisher, mock(UserManager.class)/*, remoteAppAccessorFactory*/);
        publisher.register("foo", "event.id", URI.create("/event"));

        for (int x = 0; x < 100 + 4; x++)
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
                return /* event.getAppKey().equals("foo") && */
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
