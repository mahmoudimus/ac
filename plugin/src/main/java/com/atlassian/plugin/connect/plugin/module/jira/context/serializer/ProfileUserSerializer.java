package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.connect.plugin.module.context.ResourceNotFoundException;
import com.atlassian.plugin.connect.plugin.module.permission.UnauthorisedException;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.atlassian.jira.security.Permissions.USER_PICKER;

/**
 * Serializes ProfileUser objects.
 */
public class ProfileUserSerializer extends AbstractJiraParameterSerializer<ApplicationUser, ApplicationUser>
{
    public static final String PROFILE_USER_FIELD_NAME = "profileUser";
    public static final String NAME_FIELD_NAME = "name";
    private final PermissionManager permissionManager;

    public ProfileUserSerializer(PermissionManager permissionManager, final UserManager userManager)
    {
        super(userManager, PROFILE_USER_FIELD_NAME,
                createNoopUnwrapper(ApplicationUser.class),
                new AbstractJiraStringParameterLookup<ApplicationUser>(NAME_FIELD_NAME)
                {
                    @Override
                    public ApplicationUser lookup(User user, String username)
                    {
                        return userManager.getUserByName(username);
                    }
                },
                new AbstractJiraKeyParameterLookup<ApplicationUser>()
                {
                    @Override
                    public ApplicationUser lookup(User user, String key)
                    {
                        return userManager.getUserByKey(key);
                    }
                }
        );
        this.permissionManager = permissionManager;
    }

    @Override
    public Map<String, Object> serialize(ApplicationUser applicationUser)
    {
        return ImmutableMap.<String, Object>of(PROFILE_USER_FIELD_NAME, ImmutableMap.of(
                NAME_FIELD_NAME, applicationUser.getName(),
                KEY_FIELD_NAME, applicationUser.getKey()
        ));
    }

    @Override
    protected void checkViewPermission(ApplicationUser resource, User user) throws UnauthorisedException, ResourceNotFoundException
    {
        // Users either can view all users or none
        // TODO: Can't find a way to avoid deprecated method
        if (!permissionManager.hasPermission(USER_PICKER, user))
        {
            throwResourceNotFoundException();
        }
    }

    @Override
    protected boolean isResultValid(ApplicationUser user)
    {
        return user != null;
    }
}
