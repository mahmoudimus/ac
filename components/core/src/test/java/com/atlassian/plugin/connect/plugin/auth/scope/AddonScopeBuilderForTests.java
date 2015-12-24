package com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.AddonScope;
import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.AddonScopeApiPath;
import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.RestApiScopeHelper;
import org.hamcrest.Matcher;

import java.util.Collection;

import static java.util.Arrays.asList;

public class AddonScopeBuilderForTests
{
    public static Matcher<Collection<AddonScope>> buildScopes()
    {
        AddonScopeApiPath readPaths = new AddonScopeApiPath.RestApiPath(asList(
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
        Matcher<AddonScope> readScope = new AddonScopeMatcher(ScopeName.READ.name(), asList(readPaths));
        AddonScopeApiPath writePaths = new AddonScopeApiPath.RestApiPath(asList(
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/notification.*", asList("post"), true),
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/task.*", asList("post"), true),
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/action.*", asList("post"), true),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/content.*", asList("post", "put"), true),
                new RestApiScopeHelper.RestScope("ui", asList("1", "1.0", "latest"), "/content.*", asList("post", "put"), true)
        ));
        Matcher<AddonScope> writeScope = new AddonScopeMatcher(ScopeName.WRITE.name(), asList(writePaths));
        return new AddonScopesMatcher(asList(readScope, writeScope));
    }
}
