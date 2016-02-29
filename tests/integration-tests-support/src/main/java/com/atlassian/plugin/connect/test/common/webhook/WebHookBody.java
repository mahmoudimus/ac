package com.atlassian.plugin.connect.test.common.webhook;

public interface WebHookBody {
    String find(String expression) throws Exception;

    String getConnectVersion();
}
