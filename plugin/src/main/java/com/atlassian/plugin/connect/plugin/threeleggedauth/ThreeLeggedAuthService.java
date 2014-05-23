package com.atlassian.plugin.connect.plugin.threeleggedauth;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.sal.api.user.UserKey;

public interface ThreeLeggedAuthService
{
    boolean grant(UserKey userKey, ConnectAddonBean addOnBean) throws NoUserAgencyException;
    boolean grant(UserKey userKey, ConnectAddonBean addOnBean, long expiryWindowLengthMillis) throws NoUserAgencyException;
    boolean hasGrant(UserKey userKey, ConnectAddonBean addOnBean);
    void revokeAll(String addOnKey);
    boolean shouldSilentlyIgnoreUserAgencyRequest(String username, ConnectAddonBean addOnBean); // "String username" rather than UserKey because the user may not exist or may not be active
}
