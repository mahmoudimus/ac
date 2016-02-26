package it.com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.auth.scope.AddonScopeManager;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.scopes.ScopeTestHelper;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

public abstract class ScopeManagerTest {
    private final ScopeTestHelper scopeTestHelper;
    private final Collection<ScopeTestData> scopeTestData;

    private AddonScopeManager permissionManager;
    private Map<ScopeName, Plugin> installedAddons;

    public ScopeManagerTest(AddonScopeManager scopeManager, ScopeTestHelper scopeTestHelper, Collection<ScopeTestData> scopeTestData) {
        this.permissionManager = scopeManager;
        this.scopeTestHelper = scopeTestHelper;
        this.scopeTestData = scopeTestData;
    }

    @BeforeClass
    public void setup() throws IOException {
        installedAddons = scopeTestHelper.installScopedAddons();
    }

    @AfterClass
    public void tearDown() throws IOException {
        scopeTestHelper.uninstallScopedAddons(installedAddons);
    }

    @Test
    public void testRequestsInApiScope() throws Exception {
        Iterable<Matcher<? super AddonScopeManager>> matchers = new ScopeTestDataMatcherFactory(installedAddons).toScopeTestDataMatchers(scopeTestData);
        assertThat(permissionManager, Matchers.allOf(matchers));
    }
}
