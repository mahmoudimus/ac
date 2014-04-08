package com.atlassian.plugin.connect.test.plugin;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.AddOnScopeBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.PermissionManager;
import com.atlassian.plugin.connect.plugin.PermissionManagerImpl;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonBeanFactory;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.scopes.AddOnScope;
import com.atlassian.plugin.connect.plugin.scopes.AddOnScopeApiPathBuilder;
import com.atlassian.plugin.connect.plugin.service.ScopeService;
import com.atlassian.plugin.connect.spi.PermissionDeniedException;
import com.atlassian.plugin.connect.spi.Permissions;
import com.atlassian.plugin.connect.spi.permission.Permission;
import com.atlassian.plugin.connect.spi.permission.PermissionInfo;
import com.atlassian.plugin.connect.spi.permission.PermissionModuleDescriptor;
import com.atlassian.plugin.connect.spi.permission.PermissionsReader;
import com.atlassian.plugin.connect.spi.permission.scope.ApiResourceInfo;
import com.atlassian.plugin.connect.spi.permission.scope.ApiScope;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.sal.api.user.UserKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
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
    @Mock private JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;
    @Mock private PluginModuleTracker<Permission, PermissionModuleDescriptor> pluginModuleTracker;
    @Mock private ScopeService scopeService;
    @Mock private ConnectAddonRegistry connectAddonRegistry;
    @Mock private ConnectAddonBeanFactory connectAddonBeanFactory;

    @Mock private HttpServletRequest request;
    @Mock private Plugin plugin;

    private UserKey userKey = new UserKey("a_user_key");

    @Before
    public void beforeEachTest() throws IOException
    {
        when(jsonConnectAddOnIdentifierService.isConnectAddOn(not(eq(PLUGIN_KEY)))).thenThrow(new IllegalArgumentException("Only " + PLUGIN_KEY + " is to be used as a plugin key"));
        when(pluginAccessor.getPlugin(PLUGIN_KEY)).thenReturn(plugin);
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user");
        when(request.getContextPath()).thenReturn("/jira");
        when(request.getMethod()).thenReturn("GET");
        Permission permission = createPermission();
        when(pluginModuleTracker.getModules()).thenReturn(asList(permission));
        when(scopeService.build()).thenReturn(buildTestScopes());
        permissionManager = new PermissionManagerImpl(pluginAccessor, permissionsReader, jsonConnectAddOnIdentifierService, pluginModuleTracker, scopeService, connectAddonRegistry, connectAddonBeanFactory);
    }

    @Test
    public void validXmlDescriptorPermissionIsInScopeInProdMode()
    {
        setup().withJson(false).withPermission(PERMISSION_KEY);
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(true));
    }

    @Test
    public void invalidXmlDescriptorPermissionIsOutOfScopeInProdMode()
    {
        setup().withJson(false).withPermission("wrong");
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(false));
    }

    @Test
    public void validJsonDescriptorScopeIsInScopeInProdMode()
    {
        setup().withJson(true).withScope(ScopeName.READ);
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(true));
    }

    @Test
    public void invalidJsonDescriptorScopeIsOutOfScope()
    {
        setup().withJson(true);
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(false));
    }

    @Test
    public void regexSuffixIsMatched()
    {
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user/write/something");
        when(request.getMethod()).thenReturn("POST");
        setup().withJson(true).withScope(ScopeName.WRITE);
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(true));
    }

    @Test
    public void regexSuffixIsMatchedAndInsufficientAddOnScopesAreRejected()
    {
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user/write/something");
        when(request.getMethod()).thenReturn("POST");
        setup().withJson(true).withScope(ScopeName.READ);
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(false));
    }

    @Test
    public void regexInfixIsMatched()
    {
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user/something/delete");
        when(request.getMethod()).thenReturn("DELETE");
        setup().withJson(true).withScope(ScopeName.DELETE);
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(true));
    }

    @Test
    public void regexInfixIsMatchedAndInsufficientAddOnScopesAreRejected()
    {
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user/something/delete");
        when(request.getMethod()).thenReturn("DELETE");
        setup().withJson(true).withScope(ScopeName.WRITE);
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(false));
    }

    // This test exists to ensure that signingNotVulnerableToNormalizedUris is not returning a false
    // positive test passed result. eg, if "/secure/Dashboard.jspa" suddenly becomes allowed then
    // both tests should fail
    @Test
    public void checksThatSigningVulnerabilityTestIsNotFalsePositive()
    {
        when(request.getRequestURI()).thenReturn("/jira/secure/Dashboard.jspa");
        when(request.getMethod()).thenReturn("GET");
        setup().withJson(true).withScope(ScopeName.READ);
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(false));
    }

    @Test
    public void signingNotVulnerableToNormalizedUris()
    {
        when(request.getRequestURI()).thenReturn("/jira/secure/Dashboard.jspa;../../../rest/api/2/user");
        when(request.getMethod()).thenReturn("GET");
        setup().withJson(true).withScope(ScopeName.READ);
        assertThat(permissionManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(false));
    }

    @Test
    public void jsonAddonDoesNotRequirePermissionToCreateOAuthLink()
    {
        setup().withJson(true).withScope(ScopeName.READ);
        permissionManager.requirePermission(PLUGIN_KEY, Permissions.CREATE_OAUTH_LINK);
    }

    @Test
    public void xmlAddonCanCreateOAuthLinkWithPermission()
    {
        setup().withJson(false).withPermission(Permissions.CREATE_OAUTH_LINK);
        permissionManager.requirePermission(PLUGIN_KEY, Permissions.CREATE_OAUTH_LINK);
    }

    @Test(expected = PermissionDeniedException.class)
    public void xmlAddonCantCreateOAuthLinkWithoutPermission()
    {
        setup().withJson(false);
        permissionManager.requirePermission(PLUGIN_KEY, Permissions.CREATE_OAUTH_LINK);
    }

    private Setup setup()
    {
        return new Setup();
    }

    private class Setup
    {
        private boolean isJson = false;

        Setup withJson(boolean isJson)
        {
            when(jsonConnectAddOnIdentifierService.isConnectAddOn(PLUGIN_KEY)).thenReturn(isJson);

            if (isJson)
            {
                final String mockDescriptor = buildMockDescriptor();
                when(connectAddonRegistry.getDescriptor(PLUGIN_KEY)).thenReturn(mockDescriptor);
                when(connectAddonBeanFactory.fromJsonSkipValidation(mockDescriptor)).thenReturn(buildAddOnBean(Collections.<ScopeName>emptySet()));
            }
            else
            {
                when(connectAddonRegistry.getDescriptor(PLUGIN_KEY)).thenReturn(null);
            }

            this.isJson = isJson;
            return this;
        }

        Setup withPermission(String permission)
        {
            when(permissionsReader.getPermissionsForPlugin(plugin)).thenReturn(new HashSet<String>(asList(permission)));
            return this;
        }

        Setup withScope(ScopeName scopeName)
        {
            if (!this.isJson)
            {
                throw new IllegalStateException("Cannot give a scope to a non-JSON mock add-on!");
            }

            when(connectAddonBeanFactory.fromJsonSkipValidation(buildMockDescriptor())).thenReturn(buildAddOnBean(new HashSet<ScopeName>(asList(scopeName))));
            return this;
        }

        private String buildMockDescriptor()
        {
            return String.format("~~ mock descriptor for add-on %s ~~", PLUGIN_KEY);
        }

        private ConnectAddonBean buildAddOnBean(Set<ScopeName> scopeNames)
        {
            return ConnectAddonBean.newConnectAddonBean()
                                   .withKey(PLUGIN_KEY)
                                   .withName("Mock add-on " + PLUGIN_KEY)
                                   .withBaseurl("https://example.com/" + PLUGIN_KEY)
                                   .withScopes(scopeNames)
                                   .build();
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

    private Collection<AddOnScope> buildTestScopes()
    {
        Set<AddOnScope> scopes = new HashSet<AddOnScope>();
        scopes.add(new AddOnScope(ScopeName.READ.name(), new AddOnScopeApiPathBuilder().withRestPaths(new AddOnScopeBean.RestPathBean("api", "api", asList("/user"), asList("2")), asList("get")).build()));
        scopes.add(new AddOnScope(ScopeName.WRITE.name(), new AddOnScopeApiPathBuilder().withRestPaths(new AddOnScopeBean.RestPathBean("api", "api", asList("/user/write/.+"), asList("2")), asList("post")).build()));
        scopes.add(new AddOnScope(ScopeName.DELETE.name(), new AddOnScopeApiPathBuilder().withRestPaths(new AddOnScopeBean.RestPathBean("api", "api", asList("/user/.+/delete"), asList("2")), asList("delete")).build()));
        return scopes;
    }
}