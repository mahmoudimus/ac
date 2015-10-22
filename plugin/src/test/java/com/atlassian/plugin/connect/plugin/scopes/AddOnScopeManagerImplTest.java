package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.ConnectAddonAccessor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.AddOnScopeBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.service.ScopeService;
import com.atlassian.plugin.connect.spi.scope.AddOnScope;
import com.atlassian.plugin.connect.spi.scope.AddOnScopeApiPathBuilder;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import com.atlassian.sal.api.user.UserKey;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@RunWith (MockitoJUnitRunner.class)
public class AddOnScopeManagerImplTest
{
    private static final String PLUGIN_KEY = "a plugin key";

    @InjectMocks
    private AddOnScopeManagerImpl addOnScopeManager;

    @Mock
    private ScopeService scopeService;

    @Mock
    private ConnectAddonAccessor addonAccessor;

    @Mock
    private HttpServletRequest request;
    @Mock
    private Plugin plugin;

    private UserKey userKey = new UserKey("a_user_key");

    @Before
    public void beforeEachTest() throws IOException
    {
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user");
        when(request.getContextPath()).thenReturn("/jira");
        when(request.getMethod()).thenReturn("GET");
        when(scopeService.build()).thenReturn(buildTestScopes());
        addOnScopeManager = new AddOnScopeManagerImpl(scopeService, addonAccessor);
    }

    @Test
    public void validJsonDescriptorScopeIsInScopeInProdMode()
    {
        when(addonAccessor.getAddon(PLUGIN_KEY)).thenReturn(Optional.of(buildAddOnBeanWithScopes(ScopeName.READ)));
        assertThat(addOnScopeManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(true));
    }

    @Test
    public void invalidJsonDescriptorScopeIsOutOfScope()
    {
        when(addonAccessor.getAddon(PLUGIN_KEY)).thenReturn(Optional.of(buildAddOnBeanWithScopes()));
        assertThat(addOnScopeManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(false));
    }

    @Test
    public void regexSuffixIsMatched()
    {
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user/write/something");
        when(request.getMethod()).thenReturn("POST");
        when(addonAccessor.getAddon(PLUGIN_KEY)).thenReturn(Optional.of(buildAddOnBeanWithScopes(ScopeName.WRITE)));
        assertThat(addOnScopeManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(true));
    }

    @Test
    public void regexSuffixIsMatchedAndInsufficientAddOnScopesAreRejected()
    {
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user/write/something");
        when(request.getMethod()).thenReturn("POST");
        when(addonAccessor.getAddon(PLUGIN_KEY)).thenReturn(Optional.of(buildAddOnBeanWithScopes(ScopeName.READ)));
        assertThat(addOnScopeManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(false));
    }

    @Test
    public void regexInfixIsMatched()
    {
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user/something/delete");
        when(request.getMethod()).thenReturn("DELETE");
        when(addonAccessor.getAddon(PLUGIN_KEY)).thenReturn(Optional.of(buildAddOnBeanWithScopes(ScopeName.DELETE)));
        assertThat(addOnScopeManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(true));
    }

    @Test
    public void regexInfixIsMatchedAndInsufficientAddOnScopesAreRejected()
    {
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user/something/delete");
        when(request.getMethod()).thenReturn("DELETE");
        when(addonAccessor.getAddon(PLUGIN_KEY)).thenReturn(Optional.of(buildAddOnBeanWithScopes(ScopeName.WRITE)));
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
        when(addonAccessor.getAddon(PLUGIN_KEY)).thenReturn(Optional.of(buildAddOnBeanWithScopes(ScopeName.READ)));
        assertThat(addOnScopeManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(false));
    }

    @Test
    public void signingNotVulnerableToNormalizedUris()
    {
        when(request.getRequestURI()).thenReturn("/jira/secure/Dashboard.jspa;../../../rest/api/2/user");
        when(request.getMethod()).thenReturn("GET");
        when(addonAccessor.getAddon(PLUGIN_KEY)).thenReturn(Optional.of(buildAddOnBeanWithScopes(ScopeName.READ)));
        assertThat(addOnScopeManager.isRequestInApiScope(request, PLUGIN_KEY, userKey), is(false));
    }

    private ConnectAddonBean buildAddOnBeanWithScopes(ScopeName... scopeNames)
    {
        return ConnectAddonBean.newConnectAddonBean()
                .withKey(PLUGIN_KEY)
                .withName("Mock add-on " + PLUGIN_KEY)
                .withBaseurl("https://example.com/" + PLUGIN_KEY)
                .withScopes(Sets.newHashSet(scopeNames))
                .build();
    }

    private Collection<AddOnScope> buildTestScopes()
    {
        Set<AddOnScope> scopes = new HashSet<>();
        scopes.add(new AddOnScope(ScopeName.READ.name(), new AddOnScopeApiPathBuilder().withRestPaths(new AddOnScopeBean.RestPathBean("api", "api", asList("/user"), asList("2")), asList("get")).build()));
        scopes.add(new AddOnScope(ScopeName.WRITE.name(), new AddOnScopeApiPathBuilder().withRestPaths(new AddOnScopeBean.RestPathBean("api", "api", asList("/user/write/.+"), asList("2")), asList("post")).build()));
        scopes.add(new AddOnScope(ScopeName.DELETE.name(), new AddOnScopeApiPathBuilder().withRestPaths(new AddOnScopeBean.RestPathBean("api", "api", asList("/user/.+/delete"), asList("2")), asList("delete")).build()));
        return scopes;
    }
}
