package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.jira;

import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.WebPanelParameterExtractor;
import com.google.common.base.Optional;

import java.security.Principal;
import java.util.Map;

/**
 * Extracts profile user name that will be included in webpanel's iframe url (used in user profile view).
 */
public class ProfileUserNameWebPanelParameterExtractor implements WebPanelParameterExtractor
{
    public static final String PROFILE_USER_CONTEXT_KEY = "profileUser";
    public static final String PROFILE_USER_NAME = "profile_user_name";

    @Override
    public Optional<Map.Entry<String, String[]>> extract(final Map<String, Object> context)
    {
        if (context.containsKey(PROFILE_USER_CONTEXT_KEY))
        {
            final String profileUserName  = ((Principal) context.get(PROFILE_USER_CONTEXT_KEY)).getName();

            return Optional.<Map.Entry<String, String[]>>of(new ImmutableWebPanelParameterPair(PROFILE_USER_NAME, new String[] { String.valueOf(profileUserName) }));
        }
        else
        {
            return Optional.absent();
        }
    }
}
