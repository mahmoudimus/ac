package com.atlassian.plugin.connect.api.web.context;

import com.atlassian.sal.api.user.UserProfile;

import java.util.Map;

/**
 * @since 1.0
 */
public interface ModuleContextParameters extends Map<String, String> {
    void addProfileUser(UserProfile userProfile);

    /**
     * Context understood by product components, e.g. conditions.
     */
    Map<String, ?> getOriginalContext();
}
