package com.atlassian.labs.remoteapps;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import net.oauth.OAuth;
import org.dom4j.Element;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 *
 */
public class ApiPermissionManager
{
    private final ApplicationLinkService applicationLinkService;
    private final OAuthLinkManager linkManager;

    public ApiPermissionManager(ApplicationLinkService applicationLinkService, OAuthLinkManager linkManager)
    {
        this.applicationLinkService = applicationLinkService;
        this.linkManager = linkManager;
    }

    public void setPermissions(ApplicationType type, List<String> readApis, List<String> writeApis)
    {
        ApplicationLink link = applicationLinkService.getPrimaryApplicationLink(type.getClass());
        link.putProperty("read-apis", readApis);
        link.putProperty("write-apis", writeApis);
    }

    public boolean isWritable(String consumerKey, String api)
    {
        ApplicationLink link = linkManager.getLinkForOAuthClientKey(consumerKey);
        List<String> writeApis = (List<String>) link.getProperty("write-apis");
        return writeApis != null && writeApis.contains(api);
    }

    public boolean isReadable(String consumerKey, String api)
    {
        ApplicationLink link = linkManager.getLinkForOAuthClientKey(consumerKey);
        List<String> readApis = (List<String>) link.getProperty("read-apis");
        return readApis != null && readApis.contains(api);
    }
}
