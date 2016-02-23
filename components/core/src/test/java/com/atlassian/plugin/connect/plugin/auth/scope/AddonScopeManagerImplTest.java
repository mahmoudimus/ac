package com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.ConnectAddonAccessor;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.AddonScopeBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.AddonScope;
import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.AddonScopeApiPathBuilder;
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

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ConvertToWiredTest
@RunWith(MockitoJUnitRunner.class)
public class AddonScopeManagerImplTest {
    private static final String PLUGIN_KEY = "a plugin key";

    @InjectMocks
    private AddonScopeManagerImpl addonScopeManager;

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
    public void beforeEachTest() throws IOException {
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user");
        when(request.getContextPath()).thenReturn("/jira");
        when(request.getMethod()).thenReturn("GET");
        when(scopeService.build()).thenReturn(buildTestScopes());
        addonScopeManager = new AddonScopeManagerImpl(scopeService, addonAccessor);
    }

    @Test
    public void validJsonDescriptorScopeIsInScopeInProdMode() {
        when(addonAccessor.getAddon(PLUGIN_KEY)).thenReturn(Optional.of(buildAddonBeanWithScopes(ScopeName.READ)));
        assertThat(addonScopeManager.isRequestInApiScope(request, PLUGIN_KEY), is(true));
    }

    @Test
    public void invalidJsonDescriptorScopeIsOutOfScope() {
        when(addonAccessor.getAddon(PLUGIN_KEY)).thenReturn(Optional.of(buildAddonBeanWithScopes()));
        assertThat(addonScopeManager.isRequestInApiScope(request, PLUGIN_KEY), is(false));
    }

    @Test
    public void regexSuffixIsMatched() {
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user/write/something");
        when(request.getMethod()).thenReturn("POST");
        when(addonAccessor.getAddon(PLUGIN_KEY)).thenReturn(Optional.of(buildAddonBeanWithScopes(ScopeName.WRITE)));
        assertThat(addonScopeManager.isRequestInApiScope(request, PLUGIN_KEY), is(true));
    }

    @Test
    public void regexSuffixIsMatchedAndInsufficientAddonScopesAreRejected() {
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user/write/something");
        when(request.getMethod()).thenReturn("POST");
        when(addonAccessor.getAddon(PLUGIN_KEY)).thenReturn(Optional.of(buildAddonBeanWithScopes(ScopeName.READ)));
        assertThat(addonScopeManager.isRequestInApiScope(request, PLUGIN_KEY), is(false));
    }

    @Test
    public void regexInfixIsMatched() {
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user/something/delete");
        when(request.getMethod()).thenReturn("DELETE");
        when(addonAccessor.getAddon(PLUGIN_KEY)).thenReturn(Optional.of(buildAddonBeanWithScopes(ScopeName.DELETE)));
        assertThat(addonScopeManager.isRequestInApiScope(request, PLUGIN_KEY), is(true));
    }

    @Test
    public void regexInfixIsMatchedAndInsufficientAddonScopesAreRejected() {
        when(request.getRequestURI()).thenReturn("/jira/rest/api/2/user/something/delete");
        when(request.getMethod()).thenReturn("DELETE");
        when(addonAccessor.getAddon(PLUGIN_KEY)).thenReturn(Optional.of(buildAddonBeanWithScopes(ScopeName.WRITE)));
        assertThat(addonScopeManager.isRequestInApiScope(request, PLUGIN_KEY), is(false));
    }

    // This test exists to ensure that signingNotVulnerableToNormalizedUris is not returning a false
    // positive test passed result. eg, if "/secure/Dashboard.jspa" suddenly becomes allowed then
    // both tests should fail
    @Test
    public void checksThatSigningVulnerabilityTestIsNotFalsePositive() {
        when(request.getRequestURI()).thenReturn("/jira/secure/Dashboard.jspa");
        when(request.getMethod()).thenReturn("GET");
        when(addonAccessor.getAddon(PLUGIN_KEY)).thenReturn(Optional.of(buildAddonBeanWithScopes(ScopeName.READ)));
        assertThat(addonScopeManager.isRequestInApiScope(request, PLUGIN_KEY), is(false));
    }

    @Test
    public void signingNotVulnerableToNormalizedUris() {
        when(request.getRequestURI()).thenReturn("/jira/secure/Dashboard.jspa;../../../rest/api/2/user");
        when(request.getMethod()).thenReturn("GET");
        when(addonAccessor.getAddon(PLUGIN_KEY)).thenReturn(Optional.of(buildAddonBeanWithScopes(ScopeName.READ)));
        assertThat(addonScopeManager.isRequestInApiScope(request, PLUGIN_KEY), is(false));
    }

    private ConnectAddonBean buildAddonBeanWithScopes(ScopeName... scopeNames) {
        return ConnectAddonBean.newConnectAddonBean()
                .withKey(PLUGIN_KEY)
                .withName("Mock add-on " + PLUGIN_KEY)
                .withBaseurl("https://example.com/" + PLUGIN_KEY)
                .withScopes(Sets.newHashSet(scopeNames))
                .build();
    }

    private Collection<AddonScope> buildTestScopes() {
        Set<AddonScope> scopes = new HashSet<>();
        scopes.add(new AddonScope(ScopeName.READ.name(), new AddonScopeApiPathBuilder().withRestPaths(new AddonScopeBean.RestPathBean("api", "api", singletonList("/user"), singletonList("2")), singletonList("get")).build()));
        scopes.add(new AddonScope(ScopeName.WRITE.name(), new AddonScopeApiPathBuilder().withRestPaths(new AddonScopeBean.RestPathBean("api", "api", singletonList("/user/write/.+"), singletonList("2")), singletonList("post")).build()));
        scopes.add(new AddonScope(ScopeName.DELETE.name(), new AddonScopeApiPathBuilder().withRestPaths(new AddonScopeBean.RestPathBean("api", "api", singletonList("/user/.+/delete"), singletonList("2")), singletonList("delete")).build()));
        return scopes;
    }
}
