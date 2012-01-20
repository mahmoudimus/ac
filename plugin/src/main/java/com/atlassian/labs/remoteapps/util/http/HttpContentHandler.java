package com.atlassian.labs.remoteapps.util.http;

import com.atlassian.labs.remoteapps.ContentRetrievalException;

/**
 * Created by IntelliJ IDEA.
 * User: mrdon
 * Date: 21/01/12
 * Time: 3:25 AM
 * To change this template use File | Settings | File Templates.
 */
public interface HttpContentHandler
{
    void onSuccess(String content);
    void onError(ContentRetrievalException ex);
}
