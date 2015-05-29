package com.atlassian.plugin.connect.core.scopes;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.scope.AddOnScope;
import com.atlassian.plugin.connect.spi.scope.AddOnScopeApiPath;
import com.atlassian.plugin.connect.spi.scope.helper.RestApiScopeHelper;
import org.hamcrest.Matcher;

import java.util.Collection;

import static java.util.Arrays.asList;

public class AddOnScopeBuilderForTests
{
    public static Matcher<Collection<AddOnScope>> buildScopes()
    {
        AddOnScopeApiPath readPaths = new AddOnScopeApiPath.RestApiPath(asList(
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/notification.*", asList("get"), true),
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/task.*", asList("get"), true),
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/status.*", asList("get"), true),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/search.*", asList("get"), true),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/attachment.*", asList("get"), true),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/breadcrumb.*", asList("get"), true),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/space.*", asList("get"), true),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/content.*", asList("get"), true),
                new RestApiScopeHelper.RestScope("ui", asList("1", "1.0", "latest"), "/content.*", asList("get"), true)
        ));
        Matcher<AddOnScope> readScope = new AddOnScopeMatcher(ScopeName.READ.name(), asList(readPaths));
        AddOnScopeApiPath writePaths = new AddOnScopeApiPath.RestApiPath(asList(
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/notification.*", asList("post"), true),
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/task.*", asList("post"), true),
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/action.*", asList("post"), true),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/content.*", asList("post", "put"), true),
                new RestApiScopeHelper.RestScope("ui", asList("1", "1.0", "latest"), "/content.*", asList("post", "put"), true)
        ));
        Matcher<AddOnScope> writeScope = new AddOnScopeMatcher(ScopeName.WRITE.name(), asList(writePaths));
        return new AddOnScopesMatcher(asList(readScope, writeScope));
    }
}
