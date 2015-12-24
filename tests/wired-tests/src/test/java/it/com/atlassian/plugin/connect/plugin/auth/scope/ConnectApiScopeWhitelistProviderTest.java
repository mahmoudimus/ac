package it.com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.auth.scope.AddonScopeManager;
import com.atlassian.plugin.connect.testsupport.scopes.ScopeTestHelper;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.collect.Lists;
import org.junit.runner.RunWith;

import java.util.Collection;

/**
 * This test checks that the whitelist provided by the reference plugin is properly loaded.
 */
@RunWith(AtlassianPluginsTestRunner.class)
public class ConnectApiScopeWhitelistProviderTest extends ScopeManagerTest
{

    public ConnectApiScopeWhitelistProviderTest(AddonScopeManager scopeManager,
            ScopeTestHelper scopeTestHelper)
    {
        super(scopeManager, scopeTestHelper, testData());
    }

    private static Collection<ScopeTestData> testData()
    {
        return Lists.newArrayList(
                new ScopeTestData(ScopeName.READ, HttpMethod.GET, "/rest/test-api/latest/test-path", "", true, "")
        );
    }
}
