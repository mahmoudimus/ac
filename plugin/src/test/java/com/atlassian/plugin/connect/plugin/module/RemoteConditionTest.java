package com.atlassian.plugin.connect.plugin.module;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.gzipfilter.org.apache.commons.lang.ObjectUtils;
import com.atlassian.plugin.connect.plugin.license.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.util.LocaleHelper;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessor;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.event.RemoteConditionEvent;
import com.atlassian.plugin.connect.spi.event.RemoteConditionFailedEvent;
import com.atlassian.plugin.connect.spi.event.RemoteConditionInvokedEvent;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.util.concurrent.Promises;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteConditionTest
{
    private static final String PLUGIN_KEY = "myPluginKey";

    private static final String URL = "http://foo.com/bar";

    private final CustomTypeSafeMatcher<RemoteConditionEvent> eventWithCorrectUrl =
            new CustomTypeSafeMatcher<RemoteConditionEvent>("an event with non negative elapsed time")
    {
        @Override
        public boolean matchesSafely(RemoteConditionEvent event)
        {
            return ObjectUtils.equals(event.getUrl().toString(), URL);
        }
    };

    private final CustomTypeSafeMatcher<RemoteConditionEvent> eventWithCorrectPluginKey =
            new CustomTypeSafeMatcher<RemoteConditionEvent>("an event with non negative elapsed time")
            {
                @Override
                public boolean matchesSafely(RemoteConditionEvent event)
                {
                    return ObjectUtils.equals(event.getPluginKey(), PLUGIN_KEY);
                }
            };

    private final CustomTypeSafeMatcher<RemoteConditionFailedEvent> failEventWithExpectedMessage =
            new CustomTypeSafeMatcher<RemoteConditionFailedEvent>("a fail event with expected message")
            {
                @Override
                public boolean matchesSafely(RemoteConditionFailedEvent event)
                {
                    return event.getMessage().startsWith("Unable to retrieve remote condition from plugin " + PLUGIN_KEY);
                }
            };

    private final CustomTypeSafeMatcher<RemoteConditionFailedEvent> failEventWithExpectedBadJsonMessage =
            new CustomTypeSafeMatcher<RemoteConditionFailedEvent>("a fail event with expected message")
            {
                @Override
                public boolean matchesSafely(RemoteConditionFailedEvent event)
                {
                    return event.getMessage().startsWith("Invalid JSON returned from remote condition: not json");
                }
            };

    private final CustomTypeSafeMatcher<RemoteConditionEvent> eventWithNonNegativeElapsedTime =
            new CustomTypeSafeMatcher<RemoteConditionEvent>("an event with non negative elapsed time")
            {
                @Override
                public boolean matchesSafely(RemoteConditionEvent event)
                {
                    return event.getElapsedMillisecs() >= 0l;
                }
            };

    @Mock
    private ProductAccessor productAccessor;

    @Mock
    private RemotablePluginAccessorFactory remotablePluginAccessorFactory;

    @Mock
    private RemotablePluginAccessor remotablePluginAccessor;

    @Mock
    private UserManager userManager;

    @Mock
    private TemplateRenderer templateRenderer;

    @Mock
    private LicenseRetriever licenseRetriever;

    @Mock
    private LocaleHelper localeHelper;

    @Mock
    private EventPublisher eventPublisher;

    private RemoteCondition remoteCondition;

    @Before
    public void init()
    {
        remoteCondition = new RemoteCondition(productAccessor, remotablePluginAccessorFactory, userManager, templateRenderer,
                licenseRetriever, localeHelper, eventPublisher);

        when(remotablePluginAccessorFactory.get(anyString())).thenReturn(remotablePluginAccessor);
    }

    @Test
    public void publishesInvokeEventOnSuccessfulCallToRemoteCondition()
    {
        invokeWhenSuccessfulResponse();
        verify(eventPublisher).publish(any(RemoteConditionInvokedEvent.class));
        // not sure why passing any(RemoteConditionInvokedEvent.class) is not enough to check type
        verify(eventPublisher).publish(argThat(new CustomTypeSafeMatcher<RemoteConditionEvent>(
                "an event with correct type")
        {
            @Override
            public boolean matchesSafely(RemoteConditionEvent event)
            {
                return event.getClass().equals(RemoteConditionInvokedEvent.class);
            }
        }));
    }

    @Test
    public void publishesInvokeEventWithNonNegativeElapsedOnSuccessfulCallToRemoteCondition()
    {
        invokeWhenSuccessfulResponse();
        verify(eventPublisher).publish(argThat(eventWithNonNegativeElapsedTime));
    }

    @Test
    public void publishesInvokeEventWithCorrectPluginKeyOnSuccessfulCallToRemoteCondition()
    {
        invokeWhenSuccessfulResponse();
        verify(eventPublisher).publish(argThat(eventWithCorrectPluginKey));
    }

    @Test
    public void publishesInvokeEventWithCorrectUrlOnSuccessfulCallToRemoteCondition()
    {
        invokeWhenSuccessfulResponse();
        verify(eventPublisher).publish(argThat(eventWithCorrectUrl));
    }


    @Test
    public void publishesFailedEventOnUnsuccessfulCallToRemoteCondition()
    {
        invokeWhenErrorResponse();
        verify(eventPublisher).publish(any(RemoteConditionFailedEvent.class));
        verify(eventPublisher).publish(argThat(new CustomTypeSafeMatcher<RemoteConditionEvent>(
                "an event with correct type")
        {
            @Override
            public boolean matchesSafely(RemoteConditionEvent event)
            {
                return event.getClass().equals(RemoteConditionFailedEvent.class);
            }
        }));
    }

    @Test
    public void publishesFailedEventWithCorrectMessageOnUnsuccessfulCallToRemoteCondition()
    {
        invokeWhenErrorResponse();
        verify(eventPublisher).publish(argThat(failEventWithExpectedMessage));
    }

    @Test
    public void publishesFailedEventWithNonNegativeElapsedOnUnsuccessfulCallToRemoteCondition()
    {
        invokeWhenErrorResponse();
        verify(eventPublisher).publish(argThat(eventWithNonNegativeElapsedTime));
    }

    @Test
    public void publishesFailedEventWithCorrectPluginKeyOnUnsuccessfulCallToRemoteCondition()
    {
        invokeWhenErrorResponse();
        verify(eventPublisher).publish(argThat(eventWithCorrectPluginKey));
    }

    @Test
    public void publishesFailedEventWithCorrectUrlOnUnsuccessfulCallToRemoteCondition()
    {
        invokeWhenErrorResponse();
        verify(eventPublisher).publish(argThat(eventWithCorrectUrl));
    }




    @Test
    public void publishesFailedEventOnMalformedJsonResponse()
    {
        invokeWhenMalformedJson();
        verify(eventPublisher).publish(any(RemoteConditionFailedEvent.class));
        verify(eventPublisher).publish(argThat(new CustomTypeSafeMatcher<RemoteConditionEvent>(
                "an event with correct type")
        {
            @Override
            public boolean matchesSafely(RemoteConditionEvent event)
            {
                return event.getClass().equals(RemoteConditionFailedEvent.class);
            }
        }));
    }

    @Test
    public void publishesFailedEventWithCorrectMessageOnMalformedJsonResponse()
    {
        invokeWhenMalformedJson();
        verify(eventPublisher).publish(argThat(failEventWithExpectedBadJsonMessage));
    }

    @Test
    public void publishesFailedEventWithNonNegativeElapsedOnMalformedJsonResponse()
    {
        invokeWhenMalformedJson();
        verify(eventPublisher).publish(argThat(eventWithNonNegativeElapsedTime));
    }

    @Test
    public void publishesFailedEventWithCorrectPluginKeyOnMalformedJsonResponse()
    {
        invokeWhenMalformedJson();
        verify(eventPublisher).publish(argThat(eventWithCorrectPluginKey));
    }

    @Test
    public void publishesFailedEventWithCorrectUrlOnMalformedJsonResponse()
    {
        invokeWhenMalformedJson();
        verify(eventPublisher).publish(argThat(eventWithCorrectUrl));
    }

    @SuppressWarnings("unchecked")
    private void invokeWhenSuccessfulResponse()
    {
        when(remotablePluginAccessor.executeAsync(any(HttpMethod.class), any(URI.class),
                any(Map.class), any(Map.class))).thenReturn(Promises.promise("{\"shouldDisplay\": true}"));

        invokeCondition();
    }

    @SuppressWarnings("unchecked")
    private void invokeWhenMalformedJson()
    {
        when(remotablePluginAccessor.executeAsync(any(HttpMethod.class), any(URI.class),
                any(Map.class), any(Map.class))).thenReturn(Promises.promise("not json"));

        invokeCondition();
    }

    @SuppressWarnings("unchecked")
    private void invokeWhenErrorResponse()
    {
        when(remotablePluginAccessor.executeAsync(any(HttpMethod.class), any(URI.class),
                any(Map.class), any(Map.class))).thenReturn(
                Promises.rejected(new RuntimeException("oops"), String.class));

        invokeCondition();
    }

    private void invokeCondition()
    {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("url", URL);
        params.put("pluginKey", PLUGIN_KEY);
        params.put("toHideSelector", "blah");

        remoteCondition.init(params);

        remoteCondition.shouldDisplay(new HashMap<String, Object>());
    }

}

