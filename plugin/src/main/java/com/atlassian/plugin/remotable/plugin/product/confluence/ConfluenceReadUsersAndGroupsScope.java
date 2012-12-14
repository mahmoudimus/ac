package com.atlassian.plugin.remotable.plugin.product.confluence;

import com.atlassian.plugin.remotable.api.confluence.ConfluencePermissions;
import com.atlassian.plugin.remotable.spi.permission.scope.RestApiScopeHelper;

import static java.util.Arrays.asList;

/**
 *
 */
public class ConfluenceReadUsersAndGroupsScope extends ConfluenceScope
{
    public ConfluenceReadUsersAndGroupsScope()
    {
        super(ConfluencePermissions.READ_USERS_AND_GROUPS,
        asList(
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/user", asList("get"))
            )
        );
    }
}
