package com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.AddonScope;
import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.AddonScopeApiPath;
import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.PathScopeHelper;
import com.atlassian.plugin.connect.plugin.auth.scope.whitelist.RestApiScopeHelper;
import org.hamcrest.Matcher;

import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class AddonScopeBuilderForTests {
    public static Matcher<Collection<AddonScope>> buildScopes() {
        AddonScopeApiPath readRestPaths = new AddonScopeApiPath.RestApiPath(asList(
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/notification.*", singletonList("get"), true),
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/task.*", singletonList("get"), true),
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/status.*", singletonList("get"), true),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/search.*", singletonList("get"), true),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/attachment.*", singletonList("get"), true),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/breadcrumb.*", singletonList("get"), true),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/space.*", singletonList("get"), true),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/content.*", singletonList("get"), true),
                new RestApiScopeHelper.RestScope("ui", asList("1", "1.0", "latest"), "/content.*", singletonList("get"), true)
        ));
        AddonScopeApiPath readPaths = new AddonScopeApiPath.ApiPath(asList(
                new PathScopeHelper(true, singletonList("/projects.*"), "get"),
                new PathScopeHelper(true, singletonList("/users.*"), "get"),
                new PathScopeHelper(true, singletonList("/status"), "get")
        ));
        Matcher<AddonScope> readScope = new AddonScopeMatcher(ScopeName.READ.name(), asList(readRestPaths, readPaths));
        AddonScopeApiPath writeRestPaths = new AddonScopeApiPath.RestApiPath(asList(
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/notification.*", singletonList("post"), true),
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/task.*", singletonList("post"), true),
                new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/action.*", singletonList("post"), true),
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/content.*", asList("post", "put"), true),
                new RestApiScopeHelper.RestScope("ui", asList("1", "1.0", "latest"), "/content.*", asList("post", "put"), true)
        ));
        AddonScopeApiPath writePaths = new AddonScopeApiPath.ApiPath(singletonList(
                new PathScopeHelper(true, singletonList("/scm.*"), "post")
        ));
        Matcher<AddonScope> writeScope = new AddonScopeMatcher(ScopeName.WRITE.name(), asList(writeRestPaths, writePaths));
        return new AddonScopesMatcher(asList(readScope, writeScope));
    }
}
