package com.atlassian.labs.remoteapps.test.webhook;

public interface WebHookWaiter
{
    WebHookBody waitForHook() throws Exception;
}
