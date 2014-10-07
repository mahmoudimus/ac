package com.atlassian.plugin.connect.test.plugin.scopes;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.capabilities.ConvertToWiredTest;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@ConvertToWiredTest
@RunWith(Parameterized.class)
public class ConfluenceRestScopesTest extends AbstractScopesTest
{
    /**
     * These tests are not exhaustive. They are samples across the common scopes (app links and Connect) and the
     * Confluence specific ones.
     */
    @Parameterized.Parameters(name = "Scope {0}: {1} {2} --> {3}")
    public static Collection<Object[]> testData()
    {
        return Arrays.asList(new Object[][]
        {
                {ScopeName.READ, HttpMethod.GET, "/confluence/rest/api/content/12345", true},
                {ScopeName.READ, HttpMethod.GET, "/confluence/rest/api/content/2031617/history/8/macro/7c8fd5b99609c2d1864391f15993e07a", true},
                {null, HttpMethod.GET, "/confluence/rest/api/content/12345", false},
                {ScopeName.READ, HttpMethod.POST, "/confluence/rest/api/content/12345", false},
                {ScopeName.READ, HttpMethod.PUT, "/confluence/rest/api/content/12345", false},
                {ScopeName.READ, HttpMethod.DELETE, "/confluence/rest/api/content/12345", false},
                {ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/api/content/12345", true},
                {ScopeName.WRITE, HttpMethod.PUT, "/confluence/rest/api/content/12345", true},
                {ScopeName.DELETE, HttpMethod.DELETE, "/confluence/rest/api/content/12345", true},

                {ScopeName.ADMIN, HttpMethod.GET, "/confluence/rest/applinks/2.0/applicationlink/uuid", false},

                {ScopeName.READ, HttpMethod.GET, "/confluence/rest/atlassian-connect/1/license", true},
                {null, HttpMethod.GET, "/confluence/rest/atlassian-connect/1/license", false},

                // macro cache flush
                {ScopeName.WRITE, HttpMethod.PUT, "/confluence/rest/atlassian-connect/1/macro/app/bar/foo", false},
                {ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/atlassian-connect/1/macro/app/bar/foo", false},
                {ScopeName.WRITE, HttpMethod.GET, "/confluence/rest/atlassian-connect/1/macro/app/bar/foo", false},
                {ScopeName.READ, HttpMethod.DELETE, "/confluence/rest/atlassian-connect/1/macro/app/bar/foo", false},
                {ScopeName.WRITE, HttpMethod.DELETE, "/confluence/rest/atlassian-connect/1/macro/app/bar/foo", true},

                {ScopeName.READ, HttpMethod.GET, "/confluence/rest/mywork/1/status/notification/count", true},
                {ScopeName.READ, HttpMethod.POST, "/confluence/rest/mywork/1/notification/metadata", false},
                {ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/mywork/1/notification/metadata", true},

                {ScopeName.READ, HttpMethod.GET, "/confluence/rest/prototype/1/buildInfo", true},
                {ScopeName.READ, HttpMethod.GET, "/confluence/rest/prototype/1/search", true},

                {ScopeName.READ, HttpMethod.GET, "/confluence/rest/ui/1.0/content/152453/labels", false},
                {ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/ui/1.0/content/152453/labels", false},

                {ScopeName.READ, HttpMethod.GET, "/confluence/rest/prototype/1/user/current", true},
                {ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/prototype/1/user/current", false},

                {null, HttpMethod.GET, "/confluence/rest/searchv3/1.0/search", false},
                {ScopeName.READ, HttpMethod.GET, "/confluence/rest/searchv3/1.0/search", true},

                {ScopeName.READ, HttpMethod.POST, "/confluence/rest/api/contentbody/convert/", true},

                {ScopeName.READ, HttpMethod.GET, "/confluence/rest/api/space/test/content", true},

                {ScopeName.READ, HttpMethod.GET, "/confluence/rest/prototype/1/label/44/watches", true},
                {ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/prototype/1/label/44/watches", true},
                {ScopeName.DELETE, HttpMethod.DELETE, "/confluence/rest/prototype/1/label/44/watches", true},
                {ScopeName.WRITE, HttpMethod.DELETE, "/confluence/rest/prototype/1/label/44/watches", false},

                {null, HttpMethod.GET, "/confluence/rest/create-dialog/1.0/spaces", false},
                {ScopeName.READ, HttpMethod.GET, "/confluence/rest/create-dialog/1.0/spaces", true},
                {ScopeName.WRITE, HttpMethod.POST, "/confluence/rest/create-dialog/1.0/spaces/skip-space-welcome-dialog", false},
        });
    }

    public ConfluenceRestScopesTest(ScopeName scope, HttpMethod method, String path, boolean expectedOutcome)
    {
        super(scope, method, path, "", expectedOutcome, "/confluence", "Confluence");
    }

}
