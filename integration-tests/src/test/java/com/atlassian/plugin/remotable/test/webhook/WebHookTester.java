package com.atlassian.plugin.remotable.test.webhook;

public interface WebHookTester
{
    void test(WebHookWaiter waiter) throws Exception;
}
