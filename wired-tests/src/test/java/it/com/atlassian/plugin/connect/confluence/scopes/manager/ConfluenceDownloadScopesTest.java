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
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/download/temp/", true),
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/download/attachments/", true),

                // suffix
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/download/temp/1234", true),
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/download/attachments/1234/name", true),

                // higher scopes
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.GET, "/confluence/download/temp/", true),
                emptyBodyForConfluence(ScopeName.DELETE, HttpMethod.GET, "/confluence/download/temp/", true),
                emptyBodyForConfluence(ScopeName.SPACE_ADMIN, HttpMethod.GET, "/confluence/download/temp/", true),
                emptyBodyForConfluence(ScopeName.ADMIN, HttpMethod.GET, "/confluence/download/temp/", true),

                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/download/temp/", false),
                emptyBodyForConfluence(ScopeName.DELETE, HttpMethod.POST, "/confluence/download/temp/", false),
                emptyBodyForConfluence(ScopeName.ADMIN, HttpMethod.POST, "/confluence/download/temp/", false),

                // one-thing-wrong cases
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/download/temp", false), // missing ending slash - this is what the old scopes did
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/different", false),
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/download/TEMP/", false),
                emptyBodyForConfluence(null, HttpMethod.GET, "/confluence/download/temp/", false),
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.POST, "/confluence/download/temp/", false),
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.PUT, "/confluence/download/temp/", false),
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.DELETE, "/confluencer/download/temp/", false));
    }
}
