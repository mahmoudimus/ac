package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.PermissionManagerImpl;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.PluginForTests;
import com.atlassian.plugin.connect.plugin.service.IsDevModeService;
import com.atlassian.plugin.connect.plugin.service.ScopeService;
import com.atlassian.plugin.connect.plugin.service.ScopeServiceImpl;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.spi.permission.PermissionsReader;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserKey;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractScopesTest
{
    private static final String PLUGIN_KEY = "my-plugin";
    private static final UserKey USER_KEY = null;

    private PermissionManagerImpl permissionManager;
    private PermissionsReader permissionsReader;
    private HttpServletRequest request;
    private Plugin plugin = new PluginForTests(PLUGIN_KEY, "My Plugin");

    private final ScopeName scope;
    private final HttpMethod method;
    private final String path;
    private final boolean expectedOutcome;
    private final String contextPath;
    private final String productName;


    public AbstractScopesTest(ScopeName scope, HttpMethod method, String path, boolean expectedOutcome, String contextPath, String productName)
    {
        this.scope = scope;
        this.method = method;
        this.path = path;
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

        permissionsReader = mock(PermissionsReader.class);

        Set<ScopeName> scopeSet = (null == scope) ? Sets.<ScopeName>newHashSet() : Sets.newHashSet(scope);
        when(permissionsReader.readScopesForAddOn(plugin)).thenReturn(scopeSet);

        PluginAccessor pluginAccessor = mock(PluginAccessor.class);
        when(pluginAccessor.getPlugin(PLUGIN_KEY)).thenReturn(plugin);

        IsDevModeService isDevModeService = mock(IsDevModeService.class);
        when(isDevModeService.isDevMode()).thenReturn(false);

        JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService = mock(JsonConnectAddOnIdentifierService.class);
        when(jsonConnectAddOnIdentifierService.isConnectAddOn(PLUGIN_KEY)).thenReturn(true);

        ApplicationProperties applicationProperties = mock(ApplicationProperties.class);
        when(applicationProperties.getDisplayName()).thenReturn(productName);

        PluginEventManager pluginEventManager = mock(PluginEventManager.class);
        ScopeService scopeService = new ScopeServiceImpl(applicationProperties);

        permissionManager = new PermissionManagerImpl(pluginAccessor, pluginEventManager,
                permissionsReader, isDevModeService, jsonConnectAddOnIdentifierService, scopeService);
    }

    @Test
    public void testRequestInApiScope()
    {
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, USER_KEY), is(expectedOutcome));
    }

}
