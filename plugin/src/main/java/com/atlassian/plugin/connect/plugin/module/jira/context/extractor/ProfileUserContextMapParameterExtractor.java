package com.atlassian.plugin.connect.plugin.module.jira.context.extractor;

import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.ProfileUserSerializer;
import com.google.common.base.Objects;

import java.security.Principal;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Extracts profile user parameters that can be included in webpanel's iframe url.
 */
public class ProfileUserContextMapParameterExtractor extends AbstractJiraContextMapParameterExtractor<ApplicationUser>
{
    public static final String PROFILE_USER_CONTEXT_KEY = "profileUser";
    private final UserUtil userUtil;

    // TODO: Should I be using UserManager or UserUtil
    public ProfileUserContextMapParameterExtractor(UserUtil userUtil, ProfileUserSerializer profileUserSerializer, PermissionManager permissionManager, UserManager userManager)
    {
        super(profileUserSerializer, PROFILE_USER_CONTEXT_KEY, permissionManager, userManager);
        this.userUtil = checkNotNull(userUtil);
    }

    @Override
    protected ApplicationUser getResource(Map<String, Object> context)
    {
        final Principal principal = (Principal) context.get(PROFILE_USER_CONTEXT_KEY);
        return userUtil.getUserByName(principal.getName());
    }

    @Override
    protected boolean hasPermission(PermissionManager permissionManager, ApplicationUser user, ApplicationUser contextUser, int permissionId)
    {
        // TODO: Not sure what permissions to check here
        return Objects.equal(user, contextUser);
//        return permissionManager.hasPermission(permissionId, contextUser, user);
    }

}
