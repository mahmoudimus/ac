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
import com.atlassian.plugin.connect.spi.event.RemoteConditionFailedEvent;
import com.atlassian.plugin.connect.spi.event.RemoteConditionInvokedEvent;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.util.concurrent.Promises;
import org.hamcrest.CustomMatcher;
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
        when(remotablePluginAccessor.executeAsync(any(HttpMethod.class), any(URI.class),
                any(Map.class), any(Map.class))).thenReturn(Promises.promise("foo"));

        invokeCondition();

        verify(eventPublisher).publish(any(RemoteConditionInvokedEvent.class));
    }

    @Test
    public void publishesFailedEventOnUnsuccessfulCallToRemoteCondition()
    {
        invokeWhenErrorResult();
        verify(eventPublisher).publish(any(RemoteConditionFailedEvent.class));
    }

    @Test
    public void publishesFailedEventWithMessageOnUnsuccessfulCallToRemoteCondition()
    {
        invokeWhenErrorResult();

        verify(eventPublisher).publish(argThat(
                new CustomTypeSafeMatcher<RemoteConditionFailedEvent>("a fail event with a message")
                {
                    @Override
                    public boolean matchesSafely(RemoteConditionFailedEvent event)
                    {
                        return event.getMessage() != null;
                    }
                }));
    }

    @Test
    public void publishesFailedEventWithCorrectMessageOnUnsuccessfulCallToRemoteCondition()
    {
        invokeWhenErrorResult();

        verify(eventPublisher).publish(argThat(
                new CustomTypeSafeMatcher<RemoteConditionFailedEvent>("a fail event with expected message")
                {
                    @Override
                    public boolean matchesSafely(RemoteConditionFailedEvent event)
                    {
                        return event.getMessage().startsWith("Unable to retrieve remote condition from plugin " + PLUGIN_KEY);
                    }
                }));
    }

    @Test
    public void publishesFailedEventWithNonNegativeElapsedOnUnsuccessfulCallToRemoteCondition()
    {
        invokeWhenErrorResult();

        verify(eventPublisher).publish(argThat(
                new CustomTypeSafeMatcher<RemoteConditionFailedEvent>("a fail event with non negative elapsed time")
                {
                    @Override
                    public boolean matchesSafely(RemoteConditionFailedEvent event)
                    {
                        return event.getElapsedMillisecs() >= 0l;
                    }
                }));
    }

    @Test
    public void publishesFailedEventWithCorrectPluginKeyOnUnsuccessfulCallToRemoteCondition()
    {
        invokeWhenErrorResult();

        verify(eventPublisher).publish(argThat(
                new CustomTypeSafeMatcher<RemoteConditionFailedEvent>("a fail event with non negative elapsed time")
                {
                    @Override
                    public boolean matchesSafely(RemoteConditionFailedEvent event)
                    {
                        return ObjectUtils.equals(event.getPluginKey(), PLUGIN_KEY);
                    }
                }));
    }

    @Test
    public void publishesFailedEventWithCorrectUrlOnUnsuccessfulCallToRemoteCondition()
    {
        invokeWhenErrorResult();

        verify(eventPublisher).publish(argThat(
                new CustomTypeSafeMatcher<RemoteConditionFailedEvent>("a fail event with non negative elapsed time")
                {
                    @Override
                    public boolean matchesSafely(RemoteConditionFailedEvent event)
                    {
                        return ObjectUtils.equals(event.getUrl().toString(), URL);
                    }
                }));
    }

    private void invokeWhenErrorResult()
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

class RemoteConditionFailedEventMatcher extends CustomMatcher<RemoteConditionFailedEvent>
{
    public RemoteConditionFailedEventMatcher(String description)
    {
        super(description);
    }

    @Override
    public boolean matches(Object item)
    {
        return false;
    }

}