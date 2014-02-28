package com.atlassian.plugin.connect.plugin.iframe.context;

import com.atlassian.sal.api.user.UserProfile;

import java.util.Map;

/**
 *
 */
public interface ModuleContextParameters extends Map<String, String>
{
    void addProfileUser(UserProfile userProfile);
}
