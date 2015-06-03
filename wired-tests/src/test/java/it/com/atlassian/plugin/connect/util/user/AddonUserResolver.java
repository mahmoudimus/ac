package it.com.atlassian.plugin.connect.util.user;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

public class AddonUserResolver
{
    private static final String ADD_ON_USER_KEY_PREFIX = "addon_";

    private AddonUserResolver()
    {
        throw new UnsupportedOperationException("Utility class - should not be initiated");
    }

    public static String getAddonUserKey(String addonKey, UserManager userManager)
    {
        UserProfile addonUser = userManager.getUserProfile(ADD_ON_USER_KEY_PREFIX + addonKey);
        return addonUser == null ? null : addonUser.getUserKey().getStringValue();
    }
}
