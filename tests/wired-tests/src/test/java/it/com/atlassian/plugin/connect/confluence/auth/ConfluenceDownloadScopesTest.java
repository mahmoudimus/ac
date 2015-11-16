package it.com.atlassian.plugin.connect.confluence.auth;

import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.plugin.auth.scope.AddOnScopeManager;
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
public class ConfluenceDownloadScopesTest extends ScopeManagerTest
{
    public ConfluenceDownloadScopesTest(AddOnScopeManager scopeManager, ScopeTestHelper scopeTestHelper)
    {
        super(scopeManager, scopeTestHelper, testData());
    }

    public static Collection<ScopeTestData> testData()
    {
        // this is a small scope so the test is exhaustive
        return Arrays.asList(
                // basic case
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/download/temp/", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/download/attachments/", true),

                // suffix
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/download/temp/1234", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/download/attachments/1234/name", true),

                // higher scopes
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.GET, "/confluence/download/temp/", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.DELETE, HttpMethod.GET, "/confluence/download/temp/", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.SPACE_ADMIN, HttpMethod.GET, "/confluence/download/temp/", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.ADMIN, HttpMethod.GET, "/confluence/download/temp/", true),

                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/download/temp/", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.DELETE, HttpMethod.POST, "/confluence/download/temp/", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.ADMIN, HttpMethod.POST, "/confluence/download/temp/", false),

                // one-thing-wrong cases
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/download/temp", false), // missing ending slash - this is what the old scopes did
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/different", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/download/TEMP/", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(null, HttpMethod.GET, "/confluence/download/temp/", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.POST, "/confluence/download/temp/", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.PUT, "/confluence/download/temp/", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.DELETE, "/confluencer/download/temp/", false));
    }
}
