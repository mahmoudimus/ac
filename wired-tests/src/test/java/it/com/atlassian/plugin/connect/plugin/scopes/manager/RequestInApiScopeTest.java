package it.com.atlassian.plugin.connect.plugin.scopes.manager;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.scopes.AddOnScopeManager;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.sal.api.user.UserKey;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.LifecycleBean.newLifecycleBean;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class RequestInApiScopeTest
{
    private static final Logger log = LoggerFactory.getLogger(RequestInApiScopeTest.class);
    private static final UserKey USER_KEY = null;

    private final TestPluginInstaller testPluginInstaller;
    private final Collection<ScopeTestData> scopeTestData;
    private final Map<ScopeName, Plugin> installedPlugins;

    private AddOnScopeManager permissionManager;

    public RequestInApiScopeTest(AddOnScopeManager scopeManager, TestPluginInstaller testPluginInstaller, Collection<ScopeTestData> scopeTestData)
    {
        this.permissionManager = scopeManager;
        this.testPluginInstaller = testPluginInstaller;
        this.scopeTestData = scopeTestData;

        this.installedPlugins = new HashMap<>();
    }

    private String getPluginKeyForScopeName(final ScopeName scopeName)
    {
        if (scopeName == null)
        {
            return "NO_SCOPE" + '-' + System.currentTimeMillis();
        }
        return scopeName.toString() + '-' + System.currentTimeMillis();
    }

    @BeforeClass
    public void setup() throws IOException
    {
        for (ScopeName scopeName : ScopeName.values())
        {
            ConnectAddonBean addOnBean = createAddOnBeanWithScope(scopeName);
            final Plugin addOn = testPluginInstaller.installAddon(addOnBean);
            installedPlugins.put(scopeName, addOn);
        }

        ConnectAddonBean addOnBean = createAddOnBeanWithScope(null);
        final Plugin addOn = testPluginInstaller.installAddon(addOnBean);
        installedPlugins.put(null, addOn);
    }

    @AfterClass
    public void tearDown() throws IOException
    {
        for (Plugin plugin : installedPlugins.values())
        {
            try
            {
                testPluginInstaller.uninstallAddon(plugin);
            }
            catch (IOException e)
            {
                log.error(String.format("Unable to uninstall add-on '%s'", plugin.getKey()), e);
            }
        }
    }

    @Test
    public void testRequestsInApiScope() throws Exception
    {
        Iterable<Matcher<? super AddOnScopeManager>> matchers = toScopeTestDataMatchers(scopeTestData);
        assertThat(permissionManager, Matchers.allOf(matchers));
    }

    private ConnectAddonBean createAddOnBeanWithScope(ScopeName scopeName)
    {
        final String key = getPluginKeyForScopeName(scopeName);
        ConnectAddonBeanBuilder connectAddonBeanBuilder = newConnectAddonBean()
                .withKey(key)
                .withName(key)
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(key))
                .withLicensing(true)
                .withAuthentication(newAuthenticationBean()
                        .withType(AuthenticationType.JWT)
                        .build())
                .withLifecycle(newLifecycleBean()
                        .withInstalled("/installed")
                        .build())
                .withModule("generalPages", newPageBean()
                        .withUrl("/hello-world.html")
                        .withKey("general")
                        .withName(new I18nProperty("Greeting", "greeting"))
                        .build());

        // scopes are optional so that we can have "no scopes" test classes
        if (null != scopeName)
        {
            connectAddonBeanBuilder = connectAddonBeanBuilder.withScopes(new HashSet<>(asList(scopeName)));
        }

        return connectAddonBeanBuilder.build();
    }

    private Iterable<Matcher<? super AddOnScopeManager>> toScopeTestDataMatchers(Iterable<ScopeTestData> scopeTestData)
    {
        return Iterables.transform(scopeTestData, new Function<ScopeTestData, Matcher<? super AddOnScopeManager>>()
        {
            @Override
            public Matcher<? super AddOnScopeManager> apply(final ScopeTestData data)
            {
                return performsCorrectActionForScope(data);
            }
        });
    }

    private Matcher<? super AddOnScopeManager> performsCorrectActionForScope(final ScopeTestData data)
    {
        return new TypeSafeMatcher<AddOnScopeManager>()
        {
            @Override
            protected boolean matchesSafely(final AddOnScopeManager scopeManager)
            {
                try
                {
                    return scopeManager.isRequestInApiScope(setupRequest(data), getPluginForScope(data.scope), USER_KEY) == data.expectedOutcome;
                }
                catch (IOException e)
                {
                    return false;
                }
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendValue(data.expectedOutcome).appendText("for").appendValue(data);
            }

            private HttpServletRequest setupRequest(final ScopeTestData data) throws IOException
            {
                HttpServletRequest request = mock(HttpServletRequest.class);
                when(request.getContextPath()).thenReturn(data.contextPath);
                when(request.getRequestURI()).thenReturn(data.path);
                when(request.getMethod()).thenReturn(data.method.name());
                when(request.getInputStream()).thenAnswer(new Answer<ServletInputStream>()
                {
                    @Override
                    public ServletInputStream answer(InvocationOnMock invocationOnMock) throws Throwable
                    {
                        return new ServletStringInputStream(data.requestBody);
                    }
                });
                return request;
            }
        };
    }

    private String getPluginForScope(final ScopeName scope)
    {
        return installedPlugins.get(scope).getKey();
    }

    private static class ServletStringInputStream extends ServletInputStream
    {
        private final InputStream delegate;

        public ServletStringInputStream(String content) throws IOException
        {
            this.delegate = IOUtils.toInputStream(content, "UTF-8");
        }

        @Override
        public int read() throws IOException
        {
            return delegate.read();
        }
    }

    public static class ScopeTestData
    {
        private final ScopeName scope;
        private final HttpMethod method;
        private final String path;
        private final String requestBody;
        private final boolean expectedOutcome;
        private final String contextPath;

        public ScopeTestData(final ScopeName scope, final HttpMethod method, final String path, final String requestBody, final boolean expectedOutcome, final String contextPath)
        {
            this.scope = scope;
            this.method = method;
            this.path = path;
            this.requestBody = requestBody;
            this.expectedOutcome = expectedOutcome;
            this.contextPath = contextPath;
        }
    }
}
