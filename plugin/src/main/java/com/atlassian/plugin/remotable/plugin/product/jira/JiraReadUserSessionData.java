package com.atlassian.plugin.remotable.plugin.product.jira;

import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;
import com.google.common.collect.ImmutableList;

import static java.util.Arrays.asList;

public class JiraReadUserSessionData extends JiraScope
{
    public JiraReadUserSessionData()
    {
        super(ImmutableList.<String>of(),
                asList(
                        new RestApiScopeHelper.RestScope("auth", asList("latest", "2", "2.0.alpha1"), "/session", asList("get"))
                )
        );
    }

    @Override
    public String getKey()
    {
        return "read_user_session_data";
    }
}
