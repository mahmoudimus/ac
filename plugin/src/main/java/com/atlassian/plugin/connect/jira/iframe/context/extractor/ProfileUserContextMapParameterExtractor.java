package com.atlassian.plugin.connect.jira.iframe.context.extractor;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapParameterExtractor;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.atlassian.plugin.connect.jira.iframe.context.serializer.ProfileUserSerializer;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

import com.google.common.base.Optional;

import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Extracts profile user parameters that can be included in webpanel's iframe url.
 */
@JiraComponent
public class ProfileUserContextMapParameterExtractor implements ContextMapParameterExtractor<ApplicationUser>
{
    public static final String PROFILE_USER_CONTEXT_KEY = "profileUser";

    private final UserUtil userUtil;
    private final ProfileUserSerializer profileUserSerializer;

    @Autowired
    public ProfileUserContextMapParameterExtractor(
            UserUtil userUtil,
            ProfileUserSerializer profileUserSerializer)
    {
        this.profileUserSerializer = profileUserSerializer;
        this.userUtil = checkNotNull(userUtil);
    }

    @Override
    public Optional<ApplicationUser> extract(final Map<String, Object> context)
    {
        if (context.containsKey(PROFILE_USER_CONTEXT_KEY))
        {
            final Principal principal = (Principal) context.get(PROFILE_USER_CONTEXT_KEY);
            return Optional.of(userUtil.getUserByName(principal.getName()));
        }
        return Optional.absent();
    }

    @Override
    public ParameterSerializer<ApplicationUser> serializer()
    {
        return profileUserSerializer;
    }
}
