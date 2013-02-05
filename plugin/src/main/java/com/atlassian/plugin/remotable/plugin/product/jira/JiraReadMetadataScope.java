package com.atlassian.plugin.remotable.plugin.product.jira;

import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;
import com.google.common.collect.ImmutableList;

import static java.util.Arrays.asList;

public class JiraReadMetadataScope extends JiraScope
{

    public JiraReadMetadataScope()
    {
        super(ImmutableList.<String>of(),
            asList(
                new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/issuetype", asList("get")),
                new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/issueLinkType", asList("get")),
                new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/priority", asList("get")),
                new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/resolution", asList("get")),
                new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/serverInfo", asList("get")),
                new RestApiScopeHelper.RestScope("api", asList("latest", "2", "2.0.alpha1"), "/field", asList("get"))
            )
        );
    }

    @Override
    public String getKey()
    {
        return "read_metadata";
    }
}
