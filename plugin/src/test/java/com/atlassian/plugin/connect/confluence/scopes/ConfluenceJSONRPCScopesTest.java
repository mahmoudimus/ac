package com.atlassian.plugin.connect.confluence.scopes;

import com.atlassian.plugin.connect.confluence.scope.ConfluenceScopeProvider;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.plugin.scopes.AbstractScopesTest;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@ConvertToWiredTest
@RunWith(Parameterized.class)
public class ConfluenceJSONRPCScopesTest extends AbstractScopesTest
{
    /**
     * These tests are not exhaustive. They are samples across the different scopes and API versions.
     */
    @Parameterized.Parameters(name = "Scope {0}: {1} {2} --> {3}")
    public static Collection<Object[]> testData()
    {
        return Arrays.asList(new Object[][]
        {
            {ScopeName.READ, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/convertWikiToStorageFormat", true},
            {null, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/convertWikiToStorageFormat", false},
            {ScopeName.READ, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/convertWikiToStorageFormat", true},
            {null, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/convertWikiToStorageFormat", false},
            {ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/addComment", true},
            {ScopeName.READ, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/addComment", false},
            {ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/addComment", true},
            {ScopeName.READ, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/addComment", false},
            {ScopeName.DELETE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/removePage", true},
            {ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/removePage", false},
            {ScopeName.DELETE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/removePage", true},
            {ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/removePage", false},
            {ScopeName.SPACE_ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/addPermissionsToSpace", true},
            {ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/addPermissionsToSpace", false},
            {ScopeName.SPACE_ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/addPermissionsToSpace", true},
            {ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/addPermissionsToSpace", false},
            {ScopeName.ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/addUserToGroup", false},
            {ScopeName.ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/addUserToGroup", false},
            {ScopeName.SPACE_ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/getSpacePermissionSet", true},
            {ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/getSpacePermissionSet", false},
            {ScopeName.SPACE_ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/getSpacePermissionSet", true},
            {ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/getSpacePermissionSet", false},
            {ScopeName.SPACE_ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/getSpacePermissionSets", true},
            {ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/getSpacePermissionSets", false},
            {ScopeName.SPACE_ADMIN, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v2/getSpacePermissionSets", true},
            {ScopeName.WRITE, HttpMethod.POST, "/confluence/rpc/json-rpc/confluenceservice-v1/getSpacePermissionSets", false},
        });
    }

    public ConfluenceJSONRPCScopesTest(ScopeName scope, HttpMethod method, String path, boolean expectedOutcome)
    {
        super(scope, method, path, "", expectedOutcome, "/confluence", new ConfluenceScopeProvider());
    }

}
