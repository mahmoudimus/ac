package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.jira;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.WebPanelParameterExtractor;
import com.google.common.base.Optional;

import java.security.Principal;
import java.util.Map;

import static com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.jira.ProfileUserNameWebPanelParameterExtractor.PROFILE_USER_CONTEXT_KEY;

public class ProfileUserKeyWebPanelParameterExtractor implements WebPanelParameterExtractor
{
    public static final String PROFILE_USER_KEY = "profile_user_key";

    private final UserUtil  userUtil;

    public ProfileUserKeyWebPanelParameterExtractor(UserUtil userUtil)
    {
        this.userUtil = userUtil;
    }

    @Override
    public Optional<Map.Entry<String, String[]>> extract(final Map<String, Object> context)
    {
        if (context.containsKey(PROFILE_USER_CONTEXT_KEY))
        {
            final String profileUserName  = ((Principal) context.get(PROFILE_USER_CONTEXT_KEY)).getName();
            final ApplicationUser profileUser = userUtil.getUserByName(profileUserName);
            final String profileUserKey = profileUser.getKey();

            return Optional.<Map.Entry<String, String[]>>of(new ImmutableWebPanelParameterPair(PROFILE_USER_KEY, new String[] { String.valueOf(profileUserKey) }));
        }
        else
        {
            return Optional.absent();
        }
    }
}
