package com.atlassian.plugin.connect.plugin.auth.user;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.sal.api.user.UserKey;

public interface ThreeLeggedAuthService {

    boolean hasGrant(UserKey userKey, ConnectAddonBean addonBean);

    boolean shouldSilentlyIgnoreUserAgencyRequest(String username, ConnectAddonBean addonBean); // "String username" rather than UserKey because the user may not exist or may not be active
}
