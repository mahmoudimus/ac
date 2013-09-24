package com.atlassian.plugin.connect.plugin.module.jira.context.extractor;

import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.plugin.module.context.AbstractContextMapParameterExtractor;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;

import static com.atlassian.jira.security.Permissions.USE;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Extracts issue parameters that can be included in webpanel's iframe url.
 */
public abstract class AbstractJiraContextMapParameterExtractor<P> extends AbstractContextMapParameterExtractor<P>
{
    private final PermissionManager permissionManager;

    // TODO: This may be the wrong usermanager. Maybe the one in com.atlassian.sal.api.user is the right one
    private final UserManager userManager;

    public AbstractJiraContextMapParameterExtractor(Class<P> resourceClass, ParameterSerializer<P> parameterSerializer,
                                                    String contextParameterKey, PermissionManager permissionManager,
                                                    UserManager userManager)
    {
        super(resourceClass, parameterSerializer, contextParameterKey);
        this.permissionManager = checkNotNull(permissionManager, "permissionManager is mandatory");
        this.userManager = checkNotNull(userManager, "userManager is mandatory");
    }

    @Override
    public boolean hasViewPermission(String username, P resource)
    {
        return hasPermission(permissionManager, userManager.getUserByName(username), resource, USE);
    }

    protected abstract boolean hasPermission(PermissionManager permissionManager, ApplicationUser user, P resource, int permissionId);
}
