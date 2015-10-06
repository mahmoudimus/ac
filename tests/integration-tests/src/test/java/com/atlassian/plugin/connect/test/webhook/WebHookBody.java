package com.atlassian.plugin.connect.test.webhook;

public interface WebHookBody
{
    String find(String expression) throws Exception;

    String getConnectVersion();
}
