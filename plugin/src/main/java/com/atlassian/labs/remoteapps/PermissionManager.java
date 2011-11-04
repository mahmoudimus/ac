package com.atlassian.labs.remoteapps;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.sal.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *
 */
public class PermissionManager
{
    private static final Logger log = LoggerFactory.getLogger(PermissionManager.class);
    private final ApplicationLinkService applicationLinkService;
    private final OAuthLinkManager linkManager;
    private final UserManager userManager;

    public PermissionManager(ApplicationLinkService applicationLinkService, OAuthLinkManager linkManager, UserManager userManager)
    {
        this.applicationLinkService = applicationLinkService;
        this.linkManager = linkManager;
        this.userManager = userManager;
    }

    public void setApiPermissions(ApplicationType type, List<String> readApis, List<String> writeApis)
    {
        ApplicationLink link = applicationLinkService.getPrimaryApplicationLink(type.getClass());
        link.putProperty("read-apis", readApis);
        link.putProperty("write-apis", writeApis);
    }

    public boolean isApiWritable(String consumerKey, String api)
    {
        ApplicationLink link = linkManager.getLinkForOAuthClientKey(consumerKey);
        List<String> writeApis = (List<String>) link.getProperty("write-apis");
        return writeApis != null && writeApis.contains(api);
    }

    public boolean isApiReadable(String consumerKey, String api)
    {
        ApplicationLink link = linkManager.getLinkForOAuthClientKey(consumerKey);
        List<String> readApis = (List<String>) link.getProperty("read-apis");
        return readApis != null && readApis.contains(api);
    }

    public boolean canCurrentUserAccessRemoteApp(HttpServletRequest request, String consumerKey)
    {
        return canAccessRemoteApp(userManager.getRemoteUsername(request), consumerKey);
    }

    public boolean canAccessRemoteApp(String username, String consumerKey)
    {
        return !userManager.isSystemAdmin(username);
    }

    public boolean canAccessApi(String userId, String consumerKey)
    {
        return canAccessRemoteApp(userId, consumerKey);

    }
}
