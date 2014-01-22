package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ConfluenceRestScopesTest extends AbstractScopesTest
{
    @Parameterized.Parameters(name = "Scope {0}: {1} {2} --> {3}")
    public static Collection<Object[]> testData()
    {
        return Arrays.asList(new Object[][]{
                {ScopeName.READ, HttpMethod.GET, "/confluence/rest/ui/1.0/content/12345", true},
                {null, HttpMethod.GET, "/confluence/rest/ui/1.0/content/12345", false}
        });
    }

    public ConfluenceRestScopesTest(ScopeName scope, HttpMethod method, String path, boolean expectedOutcome)
    {
        super(scope, method, path, expectedOutcome, "/confluence", "Confluence");
    }

}
