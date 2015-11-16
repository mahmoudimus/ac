package com.atlassian.plugin.connect.plugin.web.condition;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessor;
import com.atlassian.plugin.connect.api.request.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.api.web.PluggableParametersExtractor;
import com.atlassian.plugin.connect.plugin.api.LicenseStatus;
import com.atlassian.plugin.connect.plugin.lifecycle.upm.LicenseRetriever;
import com.atlassian.plugin.connect.plugin.util.IsDevModeServiceImpl;
import com.atlassian.plugin.connect.plugin.web.HostApplicationInfo;
import com.atlassian.plugin.connect.plugin.web.context.UrlVariableSubstitutorImpl;
import com.atlassian.plugin.connect.plugin.web.iframe.IFrameUriBuilderFactoryImpl;
import com.atlassian.plugin.connect.plugin.web.iframe.LocaleHelper;
import com.atlassian.plugin.connect.spi.ProductAccessor;
import com.atlassian.plugin.connect.spi.auth.user.UserPreferencesRetriever;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.util.concurrent.Promises;
import org.apache.commons.lang3.ObjectUtils;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class AddOnConditionTest
{
    private static final String ADDON_KEY = "myAddonKey";

    private static final String URL = "http://foo.com/bar?blah=1";
    private static final String URL_PATH = "/bar";

    private final CustomTypeSafeMatcher<AddOnConditionEvent> eventWithCorrectUrl = new CustomTypeSafeMatcher<AddOnConditionEvent>("an event with non negative elapsed time")
    {
        @Override
        public boolean matchesSafely(AddOnConditionEvent event)
        {
            return ObjectUtils.equals(event.getUrlPath(), URL_PATH);
        }
    };

    private final CustomTypeSafeMatcher<AddOnConditionEvent> eventWithCorrectAddonKey = new CustomTypeSafeMatcher<AddOnConditionEvent>("an event with correct addon key")
    {
        @Override
        public boolean matchesSafely(AddOnConditionEvent event)
        {
            return ObjectUtils.equals(event.getAddonKey(), ADDON_KEY);
        }
    };

    private final CustomTypeSafeMatcher<AddOnConditionFailedEvent> failEventWithExpectedMessage = new CustomTypeSafeMatcher<AddOnConditionFailedEvent>("a fail event with expected message")
    {
        @Override
        public boolean matchesSafely(AddOnConditionFailedEvent event)
        {
            return event.getMessage().equals("oops");
        }
    };

    private final CustomTypeSafeMatcher<AddOnConditionFailedEvent> failEventWithExpectedBadJsonMessage = new CustomTypeSafeMatcher<AddOnConditionFailedEvent>("a fail event with expected message")
    {
        @Override
        public boolean matchesSafely(AddOnConditionFailedEvent event)
        {
            return event.getMessage().startsWith("Malformed response from addon condition URL:");
        }
    };

    private final CustomTypeSafeMatcher<AddOnConditionEvent> eventWithNonNegativeElapsedTime = new CustomTypeSafeMatcher<AddOnConditionEvent>("an event with non negative elapsed time")
    {
        @Override
        public boolean matchesSafely(AddOnConditionEvent event)
        {
            return event.getElapsedMillisecs() >= 0l;
        }
    };

    @Mock
    private ProductAccessor productAccessor;

    @Mock
    private RemotablePluginAccessorFactory remotablePluginAccessorFactory;

    @Mock
    private PluggableParametersExtractor webFragmentModuleContextExtractor;

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

    @Mock
    private PluginRetrievalService pluginRetrievalService;

    private AddOnCondition addonCondition;

    @Before
    public void init()
    {
        final IFrameUriBuilderFactoryImpl iFrameUriBuilderFactory = new IFrameUriBuilderFactoryImpl(
                new UrlVariableSubstitutorImpl(new IsDevModeServiceImpl()),
                remotablePluginAccessorFactory,
                userManager,
                new TestHostApplicationInfo(URL, "/"),
                licenseRetriever,
                localeHelper,
                new UserPreferencesRetriever()
                {
                    @Override
                    public TimeZone getTimeZoneFor(@Nullable String userName)
                    {
                        return TimeZone.getDefault();
                    }
                },
                pluginRetrievalService);

        addonCondition = new AddOnCondition(remotablePluginAccessorFactory,
                iFrameUriBuilderFactory,
                webFragmentModuleContextExtractor,
                eventPublisher,
                pluginRetrievalService);

        when(remotablePluginAccessorFactory.getOrThrow(anyString())).thenReturn(remotablePluginAccessor);
        when(licenseRetriever.getLicenseStatus(anyString())).thenReturn(LicenseStatus.ACTIVE);
        when(localeHelper.getLocaleTag()).thenReturn("foo");

        PluginInformation pluginInformation = mock(PluginInformation.class);
        when(pluginInformation.getVersion()).thenReturn("1.2.3");

        Plugin plugin = mock(Plugin.class);
        when(plugin.getPluginInformation()).thenReturn(pluginInformation);

        when(pluginRetrievalService.getPlugin()).thenReturn(plugin);
    }

    @Test
    public void publishesInvokeEventOnSuccessfulCallToRemoteCondition()
    {
        invokeWhenSuccessfulResponse();
        verify(eventPublisher).publish(any(AddOnConditionInvokedEvent.class));
        // not sure why passing any(AddOnConditionInvokedEvent.class) is not enough to check type
        verify(eventPublisher).publish(argThat(new CustomTypeSafeMatcher<AddOnConditionEvent>(
                "an event with correct type")
        {
            @Override
            public boolean matchesSafely(AddOnConditionEvent event)
            {
                return event.getClass().equals(AddOnConditionInvokedEvent.class);
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
    public void publishesInvokeEventWithCorrectAddonKeyOnSuccessfulCallToRemoteCondition()
    {
        invokeWhenSuccessfulResponse();
        verify(eventPublisher).publish(argThat(eventWithCorrectAddonKey));
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
        verify(eventPublisher).publish(any(AddOnConditionFailedEvent.class));
        verify(eventPublisher).publish(argThat(new CustomTypeSafeMatcher<AddOnConditionEvent>(
                "an event with correct type")
        {
            @Override
            public boolean matchesSafely(AddOnConditionEvent event)
            {
                return event.getClass().equals(AddOnConditionFailedEvent.class);
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
    public void publishesFailedEventWithCorrectAddonKeyOnUnsuccessfulCallToRemoteCondition()
    {
        invokeWhenErrorResponse();
        verify(eventPublisher).publish(argThat(eventWithCorrectAddonKey));
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
        verify(eventPublisher).publish(any(AddOnConditionFailedEvent.class));
        verify(eventPublisher).publish(argThat(new CustomTypeSafeMatcher<AddOnConditionEvent>(
                "an event with correct type")
        {
            @Override
            public boolean matchesSafely(AddOnConditionEvent event)
            {
                return event.getClass().equals(AddOnConditionFailedEvent.class);
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
    public void publishesFailedEventWithCorrectAddonKeyOnMalformedJsonResponse()
    {
        invokeWhenMalformedJson();
        verify(eventPublisher).publish(argThat(eventWithCorrectAddonKey));
    }

    @Test
    public void publishesFailedEventWithCorrectUrlOnMalformedJsonResponse()
    {
        invokeWhenMalformedJson();
        verify(eventPublisher).publish(argThat(eventWithCorrectUrl));
    }

    @SuppressWarnings ("unchecked")
    private void invokeWhenSuccessfulResponse()
    {
        when(remotablePluginAccessor.executeAsync(any(HttpMethod.class), any(URI.class),
                any(Map.class), any(Map.class))).thenReturn(Promises.promise("{\"shouldDisplay\": true}"));

        invokeCondition();
    }

    @SuppressWarnings ("unchecked")
    private void invokeWhenMalformedJson()
    {
        when(remotablePluginAccessor.executeAsync(any(HttpMethod.class), any(URI.class),
                any(Map.class), any(Map.class))).thenReturn(Promises.promise("not json"));

        invokeCondition();
    }

    @SuppressWarnings ("unchecked")
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
        params.put(AddOnCondition.ADDON_KEY, ADDON_KEY);
        params.put("toHideSelector", "blah");

        addonCondition.init(params);

        addonCondition.shouldDisplay(new HashMap<String, Object>());
    }

}

class TestHostApplicationInfo implements HostApplicationInfo
{
    private final URI url;
    private final String contextPath;

    public TestHostApplicationInfo(String url, String contextPath)
    {

        this.url = URI.create(url);
        this.contextPath = contextPath;
    }

    @Override
    public URI getUrl()
    {
        return url;
    }

    @Override
    public String getContextPath()
    {
        return contextPath;
    }
}
