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
public class ConfluenceRestScopesTest extends ScopeManagerTest
{
    public ConfluenceRestScopesTest(AddOnScopeManager scopeManager, ScopeTestHelper scopeTestHelper)
    {
        super(scopeManager, scopeTestHelper, testData());
    }
    /**
     * These tests are not exhaustive. They are samples across the common scopes (app links and Connect) and the
     * Confluence specific ones.
     */
    public static Collection<ScopeTestData> testData()
    {
        return Arrays.asList(
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/api/content/12345", true),
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/api/content/2031617/history/8/macro/7c8fd5b99609c2d1864391f15993e07a", true),
                emptyBodyForConfluence(null, HttpMethod.GET, "/confluence/rest/api/content/12345", false),
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.POST, "/confluence/rest/api/content/12345", false),
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.PUT, "/confluence/rest/api/content/12345", false),
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.DELETE, "/confluence/rest/api/content/12345", false),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/api/content/12345", true),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.PUT, "/confluence/rest/api/content/12345", true),
                emptyBodyForConfluence(ScopeName.DELETE, HttpMethod.DELETE, "/confluence/rest/api/content/12345", true),

                emptyBodyForConfluence(ScopeName.ADMIN, HttpMethod.GET, "/confluence/rest/applinks/2.0/applicationlink/uuid", false),

                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/atlassian-connect/1/license", true),
                emptyBodyForConfluence(null, HttpMethod.GET, "/confluence/rest/atlassian-connect/1/license", false),

                // macro cache flush
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.PUT, "/confluence/rest/atlassian-connect/1/macro/app/bar/foo", false),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/atlassian-connect/1/macro/app/bar/foo", false),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.GET, "/confluence/rest/atlassian-connect/1/macro/app/bar/foo", false),
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.DELETE, "/confluence/rest/atlassian-connect/1/macro/app/bar/foo", false),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.DELETE, "/confluence/rest/atlassian-connect/1/macro/app/bar/foo", true),

                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/mywork/1/status/notification/count", true),
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.POST, "/confluence/rest/mywork/1/notification/metadata", false),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/mywork/1/notification/metadata", true),

                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/prototype/1/buildInfo", true),
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/prototype/1/search", true),

                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/ui/1.0/content/152453/labels", false),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/ui/1.0/content/152453/labels", false),

                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/prototype/1/user/current", true),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/prototype/1/user/current", false),

                emptyBodyForConfluence(null, HttpMethod.GET, "/confluence/rest/searchv3/1.0/search", false),
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/searchv3/1.0/search", true),

                emptyBodyForConfluence(ScopeName.READ, HttpMethod.POST, "/confluence/rest/api/contentbody/convert/", true),

                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/api/space", true),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/api/space", true),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.PUT, "/confluence/rest/api/space/12345", true),
                emptyBodyForConfluence(ScopeName.DELETE, HttpMethod.DELETE, "/confluence/rest/api/space/12345", true),

                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/prototype/1/label/44/watches", true),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/prototype/1/label/44/watches", true),
                emptyBodyForConfluence(ScopeName.DELETE, HttpMethod.DELETE, "/confluence/rest/prototype/1/label/44/watches", true),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.DELETE, "/confluence/rest/prototype/1/label/44/watches", false),

                emptyBodyForConfluence(null, HttpMethod.GET, "/confluence/rest/create-dialog/1.0/spaces", false),
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/create-dialog/1.0/spaces", true),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/create-dialog/1.0/spaces/skip-space-welcome-dialog", false),

                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/api/longtask", true),
                emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/api/longtask/1234", true),
                emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/api/longtask/1234", false));


    }

}
