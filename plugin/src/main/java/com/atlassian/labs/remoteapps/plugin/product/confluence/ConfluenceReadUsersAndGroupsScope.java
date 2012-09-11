package com.atlassian.labs.remoteapps.plugin.product.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.ConfluencePermission;
import com.atlassian.labs.remoteapps.spi.permission.scope.RestApiScopeHelper;

import static java.util.Arrays.asList;

/**
 *
 */
public class ConfluenceReadUsersAndGroupsScope extends ConfluenceScope
{
    public ConfluenceReadUsersAndGroupsScope()
    {
        super(ConfluencePermission.READ_USERS_AND_GROUPS,
        asList(
                new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/user", asList("get"))
            )
        );
    }
}
