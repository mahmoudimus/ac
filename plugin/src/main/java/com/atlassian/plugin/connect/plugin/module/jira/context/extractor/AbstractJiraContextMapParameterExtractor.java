package com.atlassian.plugin.connect.plugin.module.jira.context.extractor;

import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapParameterExtractor;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.google.common.base.Optional;

import java.util.Map;

import static com.atlassian.jira.security.Permissions.USE;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Extracts issue parameters that can be included in webpanel's iframe url.
 */
public abstract class AbstractJiraContextMapParameterExtractor<P> implements ContextMapParameterExtractor<P>
{
    private static final String ISSUE_CONTEXT_KEY = "issue";
    private ParameterSerializer<P> parameterSerializer;
    private final String contextParameterKey;
    private final PermissionManager permissionManager;
    private final UserManager userManager;

    public AbstractJiraContextMapParameterExtractor(ParameterSerializer<P> parameterSerializer,
                                                    String contextParameterKey, PermissionManager permissionManager,
                                                    UserManager userManager)
    {
        this.parameterSerializer = parameterSerializer;
        this.contextParameterKey = contextParameterKey;
        this.permissionManager = checkNotNull(permissionManager, "permissionManager is mandatory");
        this.userManager = checkNotNull(userManager, "userManager is mandatory");
    }

    @Override
    public Optional<P> extract(final Map<String, Object> context)
    {
        if (context.containsKey(contextParameterKey))
        {
            P project = getResource(context);
            return Optional.fromNullable(project);
        }
        return Optional.absent();
    }

    protected P getResource(Map<String, Object> context)
    {
        return (P) context.get(contextParameterKey);
    }

    @Override
    public ParameterSerializer<P> serializer()
    {
        return parameterSerializer;
    }

    @Override
    public boolean hasViewPermission(String username, P resource)
    {
        return hasPermission(permissionManager, userManager.getUserByName(username), resource, USE);
    }

    protected abstract boolean hasPermission(PermissionManager permissionManager, ApplicationUser user, P resource, int permissionId);
}
