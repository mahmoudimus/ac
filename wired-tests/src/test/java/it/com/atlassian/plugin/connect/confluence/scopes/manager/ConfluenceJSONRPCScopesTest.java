package it.com.atlassian.plugin.connect.confluence.scopes.manager;

import com.atlassian.plugin.connect.api.http.HttpMethod;
import com.atlassian.plugin.connect.api.scopes.AddOnScopeManager;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.testsupport.scopes.ScopeTestHelper;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;

import static it.com.atlassian.plugin.connect.confluence.scopes.manager.ConfluenceScopeTestUtil.emptyBodyForConfluence;

@Application ("confluence")
@RunWith (AtlassianPluginsTestRunner.class)
public class ConfluenceJSONRPCScopesTest extends ScopeManagerTest
{
    public ConfluenceJSONRPCScopesTest(AddOnScopeManager scopeManager,  ScopeTestHelper scopeTestHelper)
    {
        super(scopeManager, scopeTestHelper, testData());
    }
    /**
     * These tests are not exhaustive. They are samples across the different scopes and API versions.
     */
    public static Collection<ScopeTestData> testData()
    {
        return Arrays.asList(
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/convertWikiToStorageFormat", true),
                emptyBodyForConfluence(null, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/convertWikiToStorageFormat", false),
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/convertWikiToStorageFormat", true),
                emptyBodyForConfluence(null, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/convertWikiToStorageFormat", false),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/addComment", true),
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/addComment", false),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/addComment", true),
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/addComment", false),
                emptyBodyForConfluence(ScopeName.DELETE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/removePage", true),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/removePage", false),
                emptyBodyForConfluence(ScopeName.DELETE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/removePage", true),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/removePage", false),
                emptyBodyForConfluence(ScopeName.SPACE_ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/addPermissionsToSpace", true),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/addPermissionsToSpace", false),
                emptyBodyForConfluence(ScopeName.SPACE_ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/addPermissionsToSpace", true),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/addPermissionsToSpace", false),
                emptyBodyForConfluence(ScopeName.ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/addUserToGroup", false),
                emptyBodyForConfluence(ScopeName.ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/addUserToGroup", false),
                emptyBodyForConfluence(ScopeName.SPACE_ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/getSpacePermissionSet", true),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/getSpacePermissionSet", false),
                emptyBodyForConfluence(ScopeName.SPACE_ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/getSpacePermissionSet", true),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/getSpacePermissionSet", false),
                emptyBodyForConfluence(ScopeName.SPACE_ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/getSpacePermissionSets", true),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/getSpacePermissionSets", false),
                emptyBodyForConfluence(ScopeName.SPACE_ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/getSpacePermissionSets", true),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/getSpacePermissionSets", false));
    }

}
