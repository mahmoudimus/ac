package com.atlassian.labs.remoteapps.test;

/**
 * Created by IntelliJ IDEA. User: mrdon Date: 18/02/12 Time: 1:18 AM To change this template use
 * File | Settings | File Templates.
 */
public interface RemoteAppAwarePage
{
    boolean isRemoteAppLinkPresent();

    RemoteAppTestPage clickRemoteAppLink();
}
