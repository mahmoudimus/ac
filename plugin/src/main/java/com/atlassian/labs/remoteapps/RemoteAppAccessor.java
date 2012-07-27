package com.atlassian.labs.remoteapps;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.labs.remoteapps.util.http.AuthorizationGenerator;
import com.atlassian.labs.remoteapps.util.http.HttpContentHandler;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 7/26/12 Time: 2:50 PM To change this template use
 * File | Settings | File Templates.
 */
public interface RemoteAppAccessor
{
    String getKey();
    URI getDisplayUrl();

    String signGetUrl(String targetPath, Map<String, String[]> params);
    String createGetUrl(String targetPath, Map<String, String[]> params);
    Future<String> executeAsyncGet(String user, String path, Map<String, String> params,
            Map<String, String> headers, HttpContentHandler handler) throws ContentRetrievalException;


    AuthorizationGenerator getAuthorizationGenerator();

    String getName();
}
