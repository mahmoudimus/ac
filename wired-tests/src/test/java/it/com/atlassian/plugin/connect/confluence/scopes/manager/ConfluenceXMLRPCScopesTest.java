package it.com.atlassian.plugin.connect.confluence.scopes.manager;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.scopes.AddOnScopeManager;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.plugin.scopes.manager.RequestInApiScopeTest;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;

import static it.com.atlassian.plugin.connect.confluence.scopes.manager.ConfluenceScopeTestUtil.xmlBodyForConfluence;

@Application ("confluence")
@RunWith (AtlassianPluginsTestRunner.class)
public class ConfluenceXMLRPCScopesTest extends RequestInApiScopeTest
{
    public ConfluenceXMLRPCScopesTest(AddOnScopeManager scopeManager, TestPluginInstaller testPluginInstaller)
    {
        super(scopeManager, testPluginInstaller, testData());
    }
    /**
     * These tests are not exhaustive. They are samples across the different scopes and API versions.
     */
    public static Collection<ScopeTestData> testData()
    {
        return Arrays.asList(
                xmlBodyForConfluence(ScopeName.READ, "confluence2.convertWikiToStorageFormat", true),
                xmlBodyForConfluence(null, "confluence2.convertWikiToStorageFormat", false),
                xmlBodyForConfluence(ScopeName.READ, "confluence1.convertWikiToStorageFormat", true),
                xmlBodyForConfluence(null, "confluence1.convertWikiToStorageFormat", false),
                xmlBodyForConfluence(ScopeName.WRITE, "confluence2.addComment", true),
                xmlBodyForConfluence(ScopeName.READ, "confluence2.addComment", false),
                xmlBodyForConfluence(ScopeName.WRITE, "confluence1.addComment", true),
                xmlBodyForConfluence(ScopeName.READ, "confluence1.addComment", false),
                xmlBodyForConfluence(ScopeName.DELETE, "confluence2.removePage", true),
                xmlBodyForConfluence(ScopeName.WRITE, "confluence2.removePage", false),
                xmlBodyForConfluence(ScopeName.DELETE, "confluence1.removePage", true),
                xmlBodyForConfluence(ScopeName.WRITE, "confluence1.removePage", false),
                xmlBodyForConfluence(ScopeName.SPACE_ADMIN, "confluence2.addPermissionsToSpace", true),
                xmlBodyForConfluence(ScopeName.WRITE, "confluence2.addPermissionsToSpace", false),
                xmlBodyForConfluence(ScopeName.SPACE_ADMIN, "confluence1.addPermissionsToSpace", true),
                xmlBodyForConfluence(ScopeName.WRITE, "confluence1.addPermissionsToSpace", false),
                xmlBodyForConfluence(ScopeName.ADMIN, "confluence2.addUserToGroup", false),
                xmlBodyForConfluence(ScopeName.ADMIN, "confluence1.addUserToGroup", false),
                xmlBodyForConfluence(ScopeName.SPACE_ADMIN, "confluence2.getSpacePermissionSet", true),
                xmlBodyForConfluence(ScopeName.WRITE, "confluence2.getSpacePermissionSet", false),
                xmlBodyForConfluence(ScopeName.SPACE_ADMIN, "confluence1.getSpacePermissionSet", true),
                xmlBodyForConfluence(ScopeName.WRITE, "confluence1.getSpacePermissionSet", false),
                xmlBodyForConfluence(ScopeName.SPACE_ADMIN, "confluence2.getSpacePermissionSets", true),
                xmlBodyForConfluence(ScopeName.WRITE, "confluence2.getSpacePermissionSets", false),
                xmlBodyForConfluence(ScopeName.SPACE_ADMIN, "confluence1.getSpacePermissionSets", true),
                xmlBodyForConfluence(ScopeName.WRITE, "confluence1.getSpacePermissionSets", false));
    }
}
