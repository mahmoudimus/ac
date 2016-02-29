package com.atlassian.plugin.connect.test.common.webhook;

public interface WebHookWaiter {
    WebHookBody waitForHook() throws Exception;
}
