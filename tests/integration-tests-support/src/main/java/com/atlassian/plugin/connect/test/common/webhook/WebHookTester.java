package com.atlassian.plugin.connect.test.common.webhook;

public interface WebHookTester {
    void test(WebHookWaiter waiter) throws Exception;
}
