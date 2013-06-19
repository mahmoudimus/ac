package com.atlassian.plugin.remotable.test.webhook;

public interface WebHookWaiter
{
    WebHookBody waitForHook() throws Exception;
}
