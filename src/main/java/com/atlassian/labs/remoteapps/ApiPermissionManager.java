package com.atlassian.labs.remoteapps;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import org.dom4j.Element;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 *
 */
public class ApiPermissionManager
{
    private final ApplicationLinkService applicationLinkService;

    public ApiPermissionManager(ApplicationLinkService applicationLinkService)
    {
        this.applicationLinkService = applicationLinkService;
    }

    public void setPermissions(ApplicationType type, List<String> readApis, List<String> writeApis)
    {
        ApplicationLink link = applicationLinkService.getPrimaryApplicationLink(type.getClass());
        link.putProperty("read-apis", readApis);
        link.putProperty("write-apis", writeApis);
    }

    public boolean isWritable(String api)
    {
        // todo
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    public boolean isReadable(String api)
    {
        // todo
        return true;
    }
}
