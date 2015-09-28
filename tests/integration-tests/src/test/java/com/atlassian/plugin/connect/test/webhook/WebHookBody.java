package com.atlassian.plugin.connect.test.webhook;

import java.net.URI;

public interface WebHookBody
{
    String find(String expression) throws Exception;
    URI getRequestURI() throws Exception;
    String getConnectVersion();
}
