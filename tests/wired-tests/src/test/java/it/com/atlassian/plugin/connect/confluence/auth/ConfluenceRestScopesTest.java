package it.com.atlassian.plugin.connect.confluence.auth;

import com.atlassian.plugin.connect.api.request.HttpMethod;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.auth.scope.AddonScopeManager;
import com.atlassian.plugin.connect.testsupport.scopes.ScopeTestHelper;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import it.com.atlassian.plugin.connect.plugin.auth.scope.ScopeManagerTest;
import it.com.atlassian.plugin.connect.plugin.auth.scope.ScopeTestData;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;

@Application("confluence")
@RunWith(AtlassianPluginsTestRunner.class)
public class ConfluenceRestScopesTest extends ScopeManagerTest {
    public ConfluenceRestScopesTest(AddonScopeManager scopeManager, ScopeTestHelper scopeTestHelper) {
        super(scopeManager, scopeTestHelper, testData());
    }

    /**
     * These tests are not exhaustive. They are samples across the common scopes (app links and Connect) and the
     * Confluence specific ones.
     */
    public static Collection<ScopeTestData> testData() {
        return Arrays.asList(
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/api/content/12345", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/api/content/2031617/history/8/macro/7c8fd5b99609c2d1864391f15993e07a", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(null, HttpMethod.GET, "/confluence/rest/api/content/12345", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.POST, "/confluence/rest/api/content/12345", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.PUT, "/confluence/rest/api/content/12345", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.DELETE, "/confluence/rest/api/content/12345", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/api/content/12345", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.PUT, "/confluence/rest/api/content/12345", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.DELETE, HttpMethod.DELETE, "/confluence/rest/api/content/12345", true),

                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.ADMIN, HttpMethod.GET, "/confluence/rest/applinks/2.0/applicationlink/uuid", false),

                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/atlassian-connect/1/license", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(null, HttpMethod.GET, "/confluence/rest/atlassian-connect/1/license", false),

                // macro cache flush
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.PUT, "/confluence/rest/atlassian-connect/1/macro/app/bar/foo", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/atlassian-connect/1/macro/app/bar/foo", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.GET, "/confluence/rest/atlassian-connect/1/macro/app/bar/foo", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.DELETE, "/confluence/rest/atlassian-connect/1/macro/app/bar/foo", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.DELETE, "/confluence/rest/atlassian-connect/1/macro/app/bar/foo", true),

                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/mywork/1/status/notification/count", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.POST, "/confluence/rest/mywork/1/notification/metadata", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/mywork/1/notification/metadata", true),

                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/prototype/1/buildInfo", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/prototype/1/search", true),

                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/ui/1.0/content/152453/labels", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/ui/1.0/content/152453/labels", false),

                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/prototype/1/user/current", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/prototype/1/user/current", false),

                ConfluenceScopeTestUtil.emptyBodyForConfluence(null, HttpMethod.GET, "/confluence/rest/searchv3/1.0/search", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/searchv3/1.0/search", true),

                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.POST, "/confluence/rest/api/contentbody/convert/", true),

                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/api/space", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/api/space", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.PUT, "/confluence/rest/api/space/12345", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.DELETE, HttpMethod.DELETE, "/confluence/rest/api/space/12345", true),

                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/prototype/1/label/44/watches", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/prototype/1/label/44/watches", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.DELETE, HttpMethod.DELETE, "/confluence/rest/prototype/1/label/44/watches", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.DELETE, "/confluence/rest/prototype/1/label/44/watches", false),

                ConfluenceScopeTestUtil.emptyBodyForConfluence(null, HttpMethod.GET, "/confluence/rest/create-dialog/1.0/spaces", false),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/create-dialog/1.0/spaces", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/create-dialog/1.0/spaces/skip-space-welcome-dialog", false),

                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/api/longtask", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.READ, HttpMethod.GET, "/confluence/rest/api/longtask/1234", true),
                ConfluenceScopeTestUtil.emptyBodyForConfluence(ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/api/longtask/1234", false));


    }

}
