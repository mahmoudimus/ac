package com.atlassian.plugin.connect.spi.iframe.context;

import com.atlassian.plugin.connect.api.iframe.context.ModuleContextParameters;
import com.atlassian.sal.api.user.UserProfile;

import java.util.HashMap;

import static com.atlassian.plugin.connect.spi.iframe.context.AbstractModuleContextFilter.PROFILE_NAME;
import static com.atlassian.plugin.connect.spi.iframe.context.AbstractModuleContextFilter.PROFILE_KEY;

/**
 * Just a {@link java.util.HashMap}.
 */
public class HashMapModuleContextParameters extends HashMap<String, String> implements ModuleContextParameters
{
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
