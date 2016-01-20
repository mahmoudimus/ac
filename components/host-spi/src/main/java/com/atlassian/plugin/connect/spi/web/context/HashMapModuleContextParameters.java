package com.atlassian.plugin.connect.spi.web.context;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.sal.api.user.UserProfile;

import static com.atlassian.plugin.connect.spi.web.context.AbstractModuleContextFilter.PROFILE_KEY;
import static com.atlassian.plugin.connect.spi.web.context.AbstractModuleContextFilter.PROFILE_NAME;

/**
 * Just a {@link java.util.HashMap}.
 */
public class HashMapModuleContextParameters extends HashMap<String, String> implements ModuleContextParameters
{
    private final Map<String, ?> originalContext;

    public HashMapModuleContextParameters(Map<String, ?> originalContext)
    {
        this.originalContext = originalContext;
    }

    @Override
    public void addProfileUser(final UserProfile userProfile)
    {
        if (userProfile != null)
        {
            put(PROFILE_NAME, userProfile.getUsername());
            put(PROFILE_KEY, userProfile.getUserKey().getStringValue());
        }
    }

    @Override
    public Map<String, ?> getOriginalContext()
    {
        return originalContext;
    }
}
