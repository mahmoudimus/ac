package com.atlassian.plugin.connect.test.plugin;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.AddOnScopeBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.scopes.AddOnScopeManager2;
import com.atlassian.plugin.connect.plugin.scopes.AddOnScopeManagerImpl2;
import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonBeanFactory;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.scopes.AddOnScope;
import com.atlassian.plugin.connect.plugin.scopes.AddOnScopeApiPathBuilder;
import com.atlassian.plugin.connect.plugin.service.ScopeService;
import com.atlassian.plugin.connect.plugin.xmldescriptor.XmlDescriptorExploderUnitTestHelper;
import com.atlassian.sal.api.user.UserKey;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@RunWith (MockitoJUnitRunner.class)
public class AddOnScopeManagerImplTest2
{
    private static final String PLUGIN_KEY = "a plugin key";

    private AddOnScopeManager2 addOnScopeManager;

    @Mock
    private PluginAccessor pluginAccessor;
    @Mock
    private JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;
    @Mock
    private ScopeService scopeService;
    @Mock
    private ConnectAddonRegistry connectAddonRegistry;
    @Mock
    private ConnectAddonBeanFactory connectAddonBeanFactory;

    @Mock
    private HttpServletRequest request;
    @Mock
    private Plugin plugin;

    private UserKey userKey = new UserKey("a_user_key");

    @BeforeClass
    public static void beforeAnyTest()
    {
        XmlDescriptorExploderUnitTestHelper.runBeforeTests();
    }

    @Before
    public void beforeEachTest() throws IOException
    {
        when(jsonConnectAddOnIdentifierService.isConnectAddOn(not(eq(PLUGIN_KEY)))).thenThrow(new IllegalArgumentException("Only " + PLUGIN_KEY + " is to be used as a plugin key"));
        when(pluginAccessor.getPlugin(PLUGIN_KEY)).thenReturn(plugin);
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user");
        when(request.getContextPath()).thenReturn("/jira");
        when(request.getMethod()).thenReturn("GET");
        when(scopeService.build()).thenReturn(buildTestScopes());
        addOnScopeManager = new AddOnScopeManagerImpl2(scopeService, connectAddonRegistry, connectAddonBeanFactory);
    }

    @Test
    public void validJsonDescriptorScopeIsInScopeInProdMode()
    {
        setup().withScope(ScopeName.READ);
        assertThat(addOnScopeManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(true));
    }

    @Test
    public void invalidJsonDescriptorScopeIsOutOfScope()
    {
        setup();
        assertThat(addOnScopeManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(false));
    }

    @Test
    public void regexSuffixIsMatched()
    {
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user/write/something");
        when(request.getMethod()).thenReturn("POST");
        setup().withScope(ScopeName.WRITE);
        assertThat(addOnScopeManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(true));
    }

    @Test
    public void regexSuffixIsMatchedAndInsufficientAddOnScopesAreRejected()
    {
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user/write/something");
        when(request.getMethod()).thenReturn("POST");
        setup().withScope(ScopeName.READ);
        assertThat(addOnScopeManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(false));
    }

    @Test
    public void regexInfixIsMatched()
    {
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user/something/delete");
        when(request.getMethod()).thenReturn("DELETE");
        setup().withScope(ScopeName.DELETE);
        assertThat(addOnScopeManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(true));
    }

    @Test
    public void regexInfixIsMatchedAndInsufficientAddOnScopesAreRejected()
    {
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user/something/delete");
        when(request.getMethod()).thenReturn("DELETE");
        setup().withScope(ScopeName.WRITE);
        assertThat(addOnScopeManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(false));
    }

    // This test exists to ensure that signingNotVulnerableToNormalizedUris is not returning a false
    // positive test passed result. eg, if "/secure/Dashboard.jspa" suddenly becomes allowed then
    // both tests should fail
    @Test
    public void checksThatSigningVulnerabilityTestIsNotFalsePositive()
    {
        when(request.getRequestURI()).thenReturn("/jira/secure/Dashboard.jspa");
        when(request.getMethod()).thenReturn("GET");
        setup().withScope(ScopeName.READ);
        assertThat(addOnScopeManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(false));
    }

    @Test
    public void signingNotVulnerableToNormalizedUris()
    {
        when(request.getRequestURI()).thenReturn("/jira/secure/Dashboard.jspa;../../../rest/api/2/user");
        when(request.getMethod()).thenReturn("GET");
        setup().withScope(ScopeName.READ);
        assertThat(addOnScopeManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(false));
    }

    private Setup setup()
    {
        return new Setup();
    }

    private class Setup
    {
        private Setup()
        {
            when(jsonConnectAddOnIdentifierService.isConnectAddOn(PLUGIN_KEY)).thenReturn(true);

            final String mockDescriptor = buildMockDescriptor();
            when(connectAddonRegistry.getDescriptor(PLUGIN_KEY)).thenReturn(mockDescriptor);
            when(connectAddonBeanFactory.fromJsonSkipValidation(mockDescriptor)).thenReturn(buildAddOnBean(Collections.<ScopeName>emptySet()));
        }

        Setup withScope(ScopeName scopeName)
        {
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

    private Collection<AddOnScope> buildTestScopes()
    {
        Set<AddOnScope> scopes = new HashSet<AddOnScope>();
        scopes.add(new AddOnScope(ScopeName.READ.name(), new AddOnScopeApiPathBuilder().withRestPaths(new AddOnScopeBean.RestPathBean("api", "api", asList("/user"), asList("2")), asList("get")).build()));
        scopes.add(new AddOnScope(ScopeName.WRITE.name(), new AddOnScopeApiPathBuilder().withRestPaths(new AddOnScopeBean.RestPathBean("api", "api", asList("/user/write/.+"), asList("2")), asList("post")).build()));
        scopes.add(new AddOnScope(ScopeName.DELETE.name(), new AddOnScopeApiPathBuilder().withRestPaths(new AddOnScopeBean.RestPathBean("api", "api", asList("/user/.+/delete"), asList("2")), asList("delete")).build()));
        return scopes;
    }
}
