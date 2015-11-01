package com.atlassian.plugin.connect.test.webhook;

public interface WebHookTester
{
    void test(WebHookWaiter waiter) throws Exception;
}
