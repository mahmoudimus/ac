package com.atlassian.plugin.connect.plugin.iframe.context;

import com.atlassian.sal.api.user.UserProfile;

import java.util.HashMap;

/**
 * Just a {@link java.util.HashMap}.
 */
public class HashMapModuleContextParameters extends HashMap<String, String> implements ModuleContextParameters
{
    public static final String PROFILE_NAME = "profileUser.name";
    public static final String PROFILE_KEY  = "profileUser.key";

    @Override
    public void addProfileUser(final UserProfile userProfile)
    {
        if (userProfile != null)
        {
            put(PROFILE_NAME, userProfile.getUsername());
            put(PROFILE_KEY, userProfile.getUserKey().getStringValue());
        }
    }
}
