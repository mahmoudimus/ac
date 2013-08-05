package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.jira;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.remotable.plugin.module.webpanel.extractor.WebPanelParameterExtractor;
import com.google.common.collect.ImmutableMap;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

/**
 * Extracts profile user parameters that can be included in webpanel's iframe url.
 */
public class ProfileUserWebPanelParameterExtractor implements WebPanelParameterExtractor
{
    public static final String PROFILE_USER_CONTEXT_KEY = "profileUser";

    private final UserUtil userUtil;

    public ProfileUserWebPanelParameterExtractor(UserUtil userUtil)
    {
        this.userUtil = userUtil;
    }

    @Override
    public Map<String, Object> extract(final Map<String, Object> context)
    {
        if (context.containsKey(PROFILE_USER_CONTEXT_KEY))
        {
            final Principal principal = (Principal) context.get(PROFILE_USER_CONTEXT_KEY);
            final ApplicationUser profileUser = userUtil.getUserByName(principal.getName());
            return ImmutableMap.<String, Object>of("profileUser", ImmutableMap.of(
                    "name", profileUser.getName(),
                    "key", profileUser.getKey()
            ));
        }
        return Collections.emptyMap();
    }
}
