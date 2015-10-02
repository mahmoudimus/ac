package com.atlassian.plugin.connect.test.webhook;

public interface WebHookWaiter
{
    WebHookBody waitForHook() throws Exception;
}
