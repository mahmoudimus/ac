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
public class ConfluenceDownloadScopesTest extends AbstractScopesTest
{
    public ConfluenceDownloadScopesTest(ScopeName scope, HttpMethod method, String path, boolean expectedOutcome)
    {
        super(scope, method, path, "", expectedOutcome, "/confluence", new ConfluenceScopeProvider());
    }

    @Parameterized.Parameters(name = "Scope {0}: {1} {2} --> {3}")
    public static Collection<Object[]> testData()
    {
        // this is a small scope so the test is exhaustive
        return Arrays.asList(new Object[][]
        {
                // basic case
                {ScopeName.READ, HttpMethod.GET, "/confluence/download/temp/", true},
                {ScopeName.READ, HttpMethod.GET, "/confluence/download/attachments/", true},

                // suffix
                {ScopeName.READ, HttpMethod.GET, "/confluence/download/temp/1234", true},
                {ScopeName.READ, HttpMethod.GET, "/confluence/download/attachments/1234/name", true},

                // higher scopes
                {ScopeName.WRITE, HttpMethod.GET, "/confluence/download/temp/", true},
                {ScopeName.DELETE, HttpMethod.GET, "/confluence/download/temp/", true},
                {ScopeName.SPACE_ADMIN, HttpMethod.GET, "/confluence/download/temp/", true},
                {ScopeName.ADMIN, HttpMethod.GET, "/confluence/download/temp/", true},

                {ScopeName.WRITE, HttpMethod.POST, "/confluence/download/temp/", false},
                {ScopeName.DELETE, HttpMethod.POST, "/confluence/download/temp/", false},
                {ScopeName.ADMIN, HttpMethod.POST, "/confluence/download/temp/", false},

                // one-thing-wrong cases
                {ScopeName.READ, HttpMethod.GET, "/confluence/download/temp", false}, // missing ending slash - this is what the old scopes did
                {ScopeName.READ, HttpMethod.GET, "/confluence/different", false},
                {ScopeName.READ, HttpMethod.GET, "/confluence/download/TEMP/", false},
                {null, HttpMethod.GET, "/confluence/download/temp/", false},
                {ScopeName.READ, HttpMethod.POST, "/confluence/download/temp/", false},
                {ScopeName.READ, HttpMethod.PUT, "/confluence/download/temp/", false},
                {ScopeName.READ, HttpMethod.DELETE, "/confluencer/download/temp/", false}
        });
    }
}
