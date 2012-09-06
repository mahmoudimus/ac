package com.atlassian.labs.remoteapps.api.service.confluence;

import com.atlassian.labs.remoteapps.api.Promise;
import com.atlassian.xmlrpc.ServiceMethod;
import com.atlassian.xmlrpc.ServiceObject;

/**
 *
 */
@ServiceObject("confluence2")
public interface ConfluenceSpaceClient
{
    @ServiceMethod("getSpace")
    Promise<Space> getSpace(String spaceKey);
}
