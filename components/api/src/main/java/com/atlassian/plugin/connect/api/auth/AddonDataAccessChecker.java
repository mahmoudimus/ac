package com.atlassian.plugin.connect.api.auth;

import javax.annotation.Nullable;

import com.atlassian.annotations.PublicApi;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A component for checking if a user or a current add-on has access to some other add-ons data
 */
@PublicApi
public class AddonDataAccessChecker
{
    private final UserManager userManager;

    @Autowired
    public AddonDataAccessChecker(final UserManager userManager)
    {
        this.userManager = userManager;
    }

    /**
     * Will return true iff. user is sysadmin or currentAddOnKey equals addonKey
     */
    public boolean hasAccessToAddon(@Nullable final UserProfile user, @Nullable final String currentAddOnKey, String addonKey)
    {
        return currentAddOnKey != null && currentAddOnKey.equals(addonKey) || user != null && userManager.isSystemAdmin(user.getUserKey());
    }
}
