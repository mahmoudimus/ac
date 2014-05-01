package com.atlassian.plugin.connect.test.plugin.scopes;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.PermissionManagerImpl;
import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonBeanFactory;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.service.ScopeService;
import com.atlassian.plugin.connect.plugin.service.ScopeServiceImpl;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.spi.permission.PermissionsReader;
import com.atlassian.plugin.connect.test.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserKey;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
public abstract class AbstractScopesTest
{
    private static final String PLUGIN_KEY = "my-plugin";
    private static final UserKey USER_KEY = null;

    private PermissionManagerImpl permissionManager;
    private HttpServletRequest request;
    private Plugin plugin = new PluginForTests(PLUGIN_KEY, "My Plugin");

    private final ScopeName scope;
    private final HttpMethod method;
    private final String path;
    private final String requestBody;
    private final boolean expectedOutcome;
    private final String contextPath;
    private final String productName;


    public AbstractScopesTest(ScopeName scope, HttpMethod method, String path, String requestBody, boolean expectedOutcome, String contextPath, String productName)
    {
        this.scope = scope;
        this.method = method;
        this.path = path;
        this.requestBody = requestBody;
        this.expectedOutcome = expectedOutcome;
        this.contextPath = contextPath;
        this.productName = productName;
    }

    @Before
    public void setupPermissionManager() throws Exception
    {
        request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn(contextPath);
        when(request.getRequestURI()).thenReturn(path);
        when(request.getMethod()).thenReturn(method.name());
        when(request.getInputStream()).thenAnswer(new Answer<ServletInputStream>()
        {
            @Override
            public ServletInputStream answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                return new ServletStringInputStream(requestBody);
            }
        });

        PluginAccessor pluginAccessor = mock(PluginAccessor.class);
        when(pluginAccessor.getPlugin(PLUGIN_KEY)).thenReturn(plugin);

        JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService = mock(JsonConnectAddOnIdentifierService.class);
        when(jsonConnectAddOnIdentifierService.isConnectAddOn(PLUGIN_KEY)).thenReturn(true);

        ApplicationProperties applicationProperties = mock(ApplicationProperties.class);
        when(applicationProperties.getDisplayName()).thenReturn(productName);

        PluginEventManager pluginEventManager = mock(PluginEventManager.class);
        ScopeService scopeService = new ScopeServiceImpl(applicationProperties);

        Set<ScopeName> scopeSet = (null == scope) ? Sets.<ScopeName>newHashSet() : Sets.newHashSet(scope);
        ConnectAddonBean addon = ConnectAddonBean.newConnectAddonBean()
                                                 .withKey(PLUGIN_KEY)
                                                 .withName("Test add-on " + PLUGIN_KEY)
                                                 .withBaseurl("https://example.com")
                                                 .withScopes(scopeSet)
                                                 .build();
        final String mockDescriptor = String.format("~~ mock descriptor for add-on %s ~~", PLUGIN_KEY);

        final ConnectAddonRegistry connectAddonRegistry = mock(ConnectAddonRegistry.class);
        when(connectAddonRegistry.getDescriptor(PLUGIN_KEY)).thenReturn(mockDescriptor);

        final ConnectAddonBeanFactory addonBeanFactory = mock(ConnectAddonBeanFactory.class);
        when(addonBeanFactory.fromJsonSkipValidation(mockDescriptor)).thenReturn(addon);

        permissionManager = new PermissionManagerImpl(pluginAccessor, pluginEventManager,
                mock(PermissionsReader.class), jsonConnectAddOnIdentifierService, scopeService,
                connectAddonRegistry, addonBeanFactory);
    }

    @Test
    public void testRequestInApiScope()
    {
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, USER_KEY), is(expectedOutcome));
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
}
