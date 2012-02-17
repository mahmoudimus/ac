package com.atlassian.labs.remoteapps.test;

/**
 * Created by IntelliJ IDEA. User: mrdon Date: 21/02/12 Time: 12:47 AM To change this template use
 * File | Settings | File Templates.
 */
public class RemoteAppTestPage extends RemoteAppEmbeddedTestPage
{
    public RemoteAppTestPage(String pageKey)
    {
        super("servlet-" + pageKey);
    }
}
