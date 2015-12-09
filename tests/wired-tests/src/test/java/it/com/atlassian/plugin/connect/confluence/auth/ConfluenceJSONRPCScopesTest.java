package it.com.atlassian.plugin.connect.confluence.auth;

import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.plugin.auth.scope.AddonScopeManager;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.scopes.ScopeTestHelper;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.plugin.auth.scope.ScopeManagerTest;
import it.com.atlassian.plugin.connect.plugin.auth.scope.ScopeTestData;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;

@Application ("confluence")
@RunWith (AtlassianPluginsTestRunner.class)
public class ConfluenceJSONRPCScopesTest extends ScopeManagerTest
{
    public ConfluenceJSONRPCScopesTest(AddonScopeManager scopeManager,  ScopeTestHelper scopeTestHelper)
    {
        super(scopeManager, scopeTestHelper, testData());
    }
    /**
     * These tests are not exhaustive. They are samples across the different scopes and API versions.
     */
    public static Collection<ScopeTestData> testData()
    {
        return Arrays.asList(
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/convertWikiToStorageFormat", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(null, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/convertWikiToStorageFormat", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/convertWikiToStorageFormat", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(null, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/convertWikiToStorageFormat", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/addComment", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/addComment", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/addComment", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/addComment", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.DELETE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/removePage", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/removePage", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.DELETE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/removePage", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/removePage", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.SPACE_ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/addPermissionsToSpace", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/addPermissionsToSpace", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.SPACE_ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/addPermissionsToSpace", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/addPermissionsToSpace", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/addUserToGroup", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/addUserToGroup", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.SPACE_ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/getSpacePermissionSet", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/getSpacePermissionSet", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.SPACE_ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/getSpacePermissionSet", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/getSpacePermissionSet", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.SPACE_ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/getSpacePermissionSets", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/getSpacePermissionSets", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.SPACE_ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/getSpacePermissionSets", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/getSpacePermissionSets", false));
    }

}
