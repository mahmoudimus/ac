package com.atlassian.plugin.connect.confluence.scopes;

import com.atlassian.plugin.connect.confluence.scope.ConfluenceScopeProvider;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.util.annotation.ConvertToWiredTest;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.plugin.scopes.APITestUtil;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@ConvertToWiredTest
@RunWith(Parameterized.class)
public class ConfluenceXMLRPCScopesTest extends AbstractScopesTest
{
    /**
     * These tests are not exhaustive. They are samples across the different scopes and API versions.
     */
    @Parameterized.Parameters(name = "Scope {0}: {1} --> {2}")
    public static Collection<Object[]> testData()
    {
        return Arrays.asList(new Object[][]
        {
                {ScopeName.READ, "confluence2.convertWikiToStorageFormat", true},
                {null, "confluence2.convertWikiToStorageFormat", false},
                {ScopeName.READ, "confluence1.convertWikiToStorageFormat", true},
                {null, "confluence1.convertWikiToStorageFormat", false},
                {ScopeName.WRITE, "confluence2.addComment", true},
                {ScopeName.READ, "confluence2.addComment", false},
                {ScopeName.WRITE, "confluence1.addComment", true},
                {ScopeName.READ, "confluence1.addComment", false},
                {ScopeName.DELETE, "confluence2.removePage", true},
                {ScopeName.WRITE, "confluence2.removePage", false},
                {ScopeName.DELETE, "confluence1.removePage", true},
                {ScopeName.WRITE, "confluence1.removePage", false},
                {ScopeName.SPACE_ADMIN, "confluence2.addPermissionsToSpace", true},
                {ScopeName.WRITE, "confluence2.addPermissionsToSpace", false},
                {ScopeName.SPACE_ADMIN, "confluence1.addPermissionsToSpace", true},
                {ScopeName.WRITE, "confluence1.addPermissionsToSpace", false},
                {ScopeName.ADMIN, "confluence2.addUserToGroup", false},
                {ScopeName.ADMIN, "confluence1.addUserToGroup", false},
                {ScopeName.SPACE_ADMIN, "confluence2.getSpacePermissionSet", true},
                {ScopeName.WRITE, "confluence2.getSpacePermissionSet", false},
                {ScopeName.SPACE_ADMIN, "confluence1.getSpacePermissionSet", true},
                {ScopeName.WRITE, "confluence1.getSpacePermissionSet", false},
                {ScopeName.SPACE_ADMIN, "confluence2.getSpacePermissionSets", true},
                {ScopeName.WRITE, "confluence2.getSpacePermissionSets", false},
                {ScopeName.SPACE_ADMIN, "confluence1.getSpacePermissionSets", true},
                {ScopeName.WRITE, "confluence1.getSpacePermissionSets", false},
        });
    }

    public ConfluenceXMLRPCScopesTest(ScopeName scope, String methodName, boolean expectedOutcome)
    {
        super(scope, HttpMethod.POST, "/confluence/rpc/xmlrpc", APITestUtil.createXmlRpcPayload(methodName), expectedOutcome, "/confluence", new ConfluenceScopeProvider());
    }
}
