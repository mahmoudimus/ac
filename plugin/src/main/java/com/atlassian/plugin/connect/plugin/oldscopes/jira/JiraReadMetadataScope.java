package com.atlassian.plugin.connect.plugin.oldscopes.jira;

import com.atlassian.plugin.connect.api.jira.JiraPermissions;
import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;

import com.google.common.collect.ImmutableList;

import static java.util.Arrays.asList;

public final class JiraReadMetadataScope extends JiraScope
{
    public JiraReadMetadataScope()
    {
        super(JiraPermissions.READ_METADATA,
                ImmutableList.<String>of(),
                asList(
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/field", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/issueLinkType", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/issuetype", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/priority", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/resolution", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/serverInfo", asList("get")),
                        new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/status", asList("get"))
                )
        );
    }
}
