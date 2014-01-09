package com.atlassian.plugin.connect.plugin;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.scopes.ScopeName;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.service.IsDevModeService;
import com.atlassian.plugin.connect.spi.permission.Permission;
import com.atlassian.plugin.connect.spi.permission.PermissionInfo;
import com.atlassian.plugin.connect.spi.permission.PermissionModuleDescriptor;
import com.atlassian.plugin.connect.spi.permission.PermissionsReader;
import com.atlassian.plugin.connect.spi.permission.scope.ApiResourceInfo;
import com.atlassian.plugin.connect.spi.permission.scope.ApiScope;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PermissionManagerImplTest
{
    private static final String PERMISSION_KEY = "a permission";
    private static final String PLUGIN_KEY = "a plugin key";

    private PermissionManager permissionManager;

    @Mock private PluginAccessor pluginAccessor;
    @Mock private PermissionsReader permissionsReader;
    @Mock private IsDevModeService isDevModeService;
    @Mock private JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;
    @Mock private PluginModuleTracker<Permission, PermissionModuleDescriptor> pluginModuleTracker;
    @Mock private ApplicationProperties applicationProperties;

    @Mock private HttpServletRequest request;
    @Mock private Plugin plugin;
    private UserKey userKey = new UserKey("a_user_key");

    @Before
    public void beforeEachTest() throws IOException
    {
        when(jsonConnectAddOnIdentifierService.isConnectAddOn(not(eq(PLUGIN_KEY)))).thenThrow(new IllegalArgumentException("Only " + PLUGIN_KEY + " is to be used as a plugin key"));
        when(pluginAccessor.getPlugin(PLUGIN_KEY)).thenReturn(plugin);
        when(applicationProperties.getDisplayName()).thenReturn("jira");
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user");
        when(request.getContextPath()).thenReturn("/jira");
        when(request.getMethod()).thenReturn("GET");
        Permission permission = createPermission();
        when(pluginModuleTracker.getModules()).thenReturn(Arrays.asList(permission));
        permissionManager = new PermissionManagerImpl(pluginAccessor, permissionsReader, isDevModeService, jsonConnectAddOnIdentifierService, pluginModuleTracker, applicationProperties);
    }

    @Test
    public void validXmlDescriptorPermissionIsInScopeInDevMode()
    {
        setup().withJson(false).withPermission(PERMISSION_KEY).withDevMode(true);
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(true));
    }

    @Test
    public void validXmlDescriptorPermissionIsInScopeInProdMode()
    {
        setup().withJson(false).withPermission(PERMISSION_KEY).withDevMode(false);
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(true));
    }

    @Test
    public void invalidXmlDescriptorPermissionIsOutOfScopeInDevMode()
    {
        setup().withJson(false).withPermission("wrong").withDevMode(true);
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(false));
    }

    @Test
    public void invalidXmlDescriptorPermissionIsOutOfScopeInProdMode()
    {
        setup().withJson(false).withPermission("wrong").withDevMode(false);
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(false));
    }

    @Test
    public void validJsonDescriptorPermissionIsInScopeInDevMode()
    {
        setup().withJson(true).withScope(ScopeName.READ).withDevMode(true);
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(true));
    }

    @Test
    public void validJsonDescriptorScopeIsInScopeInProdMode()
    {
        setup().withJson(true).withScope(ScopeName.READ).withDevMode(false);
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(true));
    }

    // ACDEV-679
    @Test
    public void invalidJsonDescriptorScopeIsInScopeInDevMode()
    {
        setup().withJson(true).withDevMode(true);
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(true));
    }

    @Test
    public void invalidJsonDescriptorScopeIsOutOfScopeInProdMode()
    {
        setup().withJson(true).withDevMode(false);
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(false));
    }

    private Setup setup()
    {
        return new Setup();
    }

    private class Setup
    {
        Setup withDevMode(boolean isDevMode)
        {
            when(isDevModeService.isDevMode()).thenReturn(isDevMode);
            return this;
        }

        Setup withJson(boolean isJson)
        {
            when(jsonConnectAddOnIdentifierService.isConnectAddOn(PLUGIN_KEY)).thenReturn(isJson);
            return this;
        }

        Setup withPermission(String permission)
        {
            when(permissionsReader.getPermissionsForPlugin(plugin)).thenReturn(new HashSet<String>(Arrays.asList(permission)));
            return this;
        }

        Setup withScope(ScopeName scopeName)
        {
            when(permissionsReader.readScopesForAddOn(plugin)).thenReturn(new HashSet<ScopeName>(Arrays.asList(scopeName)));
            return this;
        }
    }

    private ApiScope createPermission()
    {
        return new ApiScope()
        {
            @Override
            public boolean allow(HttpServletRequest request, @Nullable UserKey user)
            {
                return request == PermissionManagerImplTest.this.request;
            }

            @Override
            public Iterable<ApiResourceInfo> getApiResourceInfos()
            {
                return null;
            }

            @Override
            public String getKey()
            {
                return PERMISSION_KEY;
            }

            @Override
            public PermissionInfo getPermissionInfo()
            {
                return null;
            }

            @Override
            public String getName()
            {
                return PERMISSION_KEY;
            }

            @Override
            public String getDescription()
            {
                return null;
            }
        };
    }
}
