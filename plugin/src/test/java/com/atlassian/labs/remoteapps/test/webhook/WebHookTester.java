package com.atlassian.labs.remoteapps.test.webhook;

public interface WebHookTester
{
    void test(WebHookWaiter waiter) throws Exception;
}
