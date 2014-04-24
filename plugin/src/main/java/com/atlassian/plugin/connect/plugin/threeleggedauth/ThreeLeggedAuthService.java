package com.atlassian.plugin.connect.plugin.threeleggedauth;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.sal.api.user.UserKey;

// ideally this interface and its implementations would live in a lib accessible to both atlassian-connect and atlassian-jwt
public interface ThreeLeggedAuthService
{
    boolean grant(UserKey userKey, ConnectAddonBean addOnBean) throws NoAgentScopeException;
    boolean hasGrant(UserKey userKey, ConnectAddonBean addOnBean);
    void revokeAll(ConnectAddonBean addOnBean);
}
