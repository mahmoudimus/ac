package com.atlassian.plugin.connect.plugin.auth.user;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.user.UserKey;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@ExportAsDevService
public class AddonSpecificImpersonationService implements ThreeLeggedAuthService
{
    private static String[] AUTHORISED_ADD_ON_KEYS;
    private static final String EAZYBI_ADD_ON_KEY = "com.eazybi.atlassian-connect.eazybi-jira";
    private static final String ADD_ON_KEYS_SYS_PROP = "com.atlassian.connect.3la.authorised_add_on_keys"; // comma separated list of add-on keys

    static
    {
        setAuthorisedAddonKeys(System.getProperty(ADD_ON_KEYS_SYS_PROP, EAZYBI_ADD_ON_KEY));
    }

    private static void setAuthorisedAddonKeys(String authorisedAddons)
    {
        AUTHORISED_ADD_ON_KEYS = StringUtils.split(authorisedAddons, ",");
        Arrays.sort(AUTHORISED_ADD_ON_KEYS);
    }

    @Override
    public boolean hasGrant(UserKey userKey, ConnectAddonBean addonBean)
    {
        return hasGrant(addonBean);
    }

    @Override
    public boolean shouldSilentlyIgnoreUserAgencyRequest(String username, ConnectAddonBean addonBean)
    {
        return !hasGrant(addonBean);
    }

    private boolean hasGrant(ConnectAddonBean addonBean)
    {
        return Arrays.binarySearch(AUTHORISED_ADD_ON_KEYS, addonBean.getKey()) >= 0;
    }
}
