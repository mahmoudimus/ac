package it.com.atlassian.plugin.connect.jira.scopes.manager;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.scopes.AddOnScopeManager;
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

public abstract class ScopeManagerTest
{
    private final ScopeTestHelper scopeTestHelper;
    private final Collection<ScopeTestData> scopeTestData;

    private AddOnScopeManager permissionManager;
    private Map<ScopeName, Plugin> installedAddOns;

    public ScopeManagerTest(AddOnScopeManager scopeManager, ScopeTestHelper scopeTestHelper, Collection<ScopeTestData> scopeTestData)
    {
        this.permissionManager = scopeManager;
        this.scopeTestHelper = scopeTestHelper;
        this.scopeTestData = scopeTestData;
    }

    @BeforeClass
    public void setup() throws IOException
    {
        installedAddOns = scopeTestHelper.installScopedAddOns();
    }

    @AfterClass
    public void tearDown() throws IOException
    {
        scopeTestHelper.uninstallScopedAddOns(installedAddOns);
    }

    @Test
    public void testRequestsInApiScope() throws Exception
    {
        Iterable<Matcher<? super AddOnScopeManager>> matchers = new ScopeTestDataMatcherFactory(installedAddOns).toScopeTestDataMatchers(scopeTestData);
        assertThat(permissionManager, Matchers.allOf(matchers));
    }
}
