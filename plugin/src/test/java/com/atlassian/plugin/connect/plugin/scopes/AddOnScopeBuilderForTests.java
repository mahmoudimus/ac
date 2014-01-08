package com.atlassian.plugin.connect.plugin.scopes;

import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;
import org.hamcrest.Matcher;

import java.util.Collection;

import static java.util.Arrays.asList;

public class AddOnScopeBuilderForTests
{
    public static Matcher<Collection<AddOnScope>> buildReadScope()
    {
        AddOnScopeApiPath path = new AddOnScopeApiPath.RestApiPath(asList(
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/notification", asList("get")),
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/status", asList("get")),
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/task", asList("get")),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/search", asList("get")),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/content", asList("get")),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/attachment", asList("get")),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/breadcrumb", asList("get")),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/space", asList("get")),
                new RestApiScopeHelper.RestScope("ui", asList("1", "1.0", "latest"), "/content", asList("get"))
        ));
        Matcher<AddOnScope> readScope = new AddOnScopeMatcher("READ", asList(path));
        return new AddOnScopesMatcher(asList(readScope));
    }
}