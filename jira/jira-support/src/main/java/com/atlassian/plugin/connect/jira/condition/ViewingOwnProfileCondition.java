package com.atlassian.plugin.connect.jira.condition;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * Workaround for AC-782 / JRA-35023
 * See PlugableUserProfileFragment#getFragmentHtml in JIRA for reference
 */
public class ViewingOwnProfileCondition implements Condition {

    private static final String PROFILE_USER = "profileUser";
    private static final String CURRENT_USER = "currentUser";

    @Override
    public void init(Map<String, String> context) throws PluginParseException {
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context) {
        if (context.containsKey(PROFILE_USER) && context.containsKey(CURRENT_USER)) {
            User profileUser = (User) context.get(PROFILE_USER);
            User currentUser = (User) context.get(CURRENT_USER);
            return profileUser != null && profileUser.equals(currentUser);
        }
        return false;
    }
}
