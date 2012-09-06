package com.atlassian.labs.remoteapps.api.service.confluence;

import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.xmlrpc.ServiceMethod;
import com.atlassian.xmlrpc.ServiceObject;

/**
 *
 */
@ServiceObject("confluence2")
public interface ConfluencePageClient
{
    @ServiceMethod("getPage")
    Promise<Page> getPage(Long pageId);

    @ServiceMethod("getPage")
    Promise<Page> getPage(String spaceKey, String pageTitle);
}
