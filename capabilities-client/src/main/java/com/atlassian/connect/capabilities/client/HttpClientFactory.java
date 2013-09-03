package com.atlassian.connect.capabilities.client;

import org.apache.http.client.HttpClient;

/**
 * @since version
 */
public interface HttpClientFactory
{
    HttpClient createHttpClient();
}
