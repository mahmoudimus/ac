package com.atlassian.plugin.connect.core.threeleggedauth;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.user.UserKey;
import org.apache.velocity.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@ExportAsDevService
public class AddOnSpecificImpersonationService implements ThreeLeggedAuthService
{
    private static String[] AUTHORISED_ADD_ON_KEYS;
    private static final String EAZYBI_ADD_ON_KEY = "com.eazybi.atlassian-connect.eazybi-jira";
    private static final String ADD_ON_KEYS_SYS_PROP = "com.atlassian.connect.3la.authorised_add_on_keys"; // comma separated list of add-on keys

    static
    {
        setAuthorisedAddOnKeys(System.getProperty(ADD_ON_KEYS_SYS_PROP, EAZYBI_ADD_ON_KEY));
    }

    private static void setAuthorisedAddOnKeys(String authorisedAddons)
    {
        AUTHORISED_ADD_ON_KEYS = StringUtils.split(authorisedAddons, ",");
        Arrays.sort(AUTHORISED_ADD_ON_KEYS);
    }


    @Override
    public boolean grant(UserKey userKey, ConnectAddonBean addOnBean) throws NoUserAgencyException
    {
        return false;
    }

    @Override
    public boolean grant(UserKey userKey, ConnectAddonBean addOnBean, long expiryWindowLengthMillis) throws NoUserAgencyException
    {
        return false;
    }

    @Override
    public boolean hasGrant(UserKey userKey, ConnectAddonBean addOnBean)
    {
        return hasGrant(addOnBean);
    }

    @Override
    public void revokeAll(String addOnKey)
    {
    }

    @Override
    public boolean shouldSilentlyIgnoreUserAgencyRequest(String username, ConnectAddonBean addOnBean)
    {
        return !hasGrant(addOnBean);
    }

    private boolean hasGrant(ConnectAddonBean addOnBean)
    {
        return Arrays.binarySearch(AUTHORISED_ADD_ON_KEYS, addOnBean.getKey()) >= 0;
    }
}
