package com.atlassian.connect.capabilities.client;

import org.apache.http.client.ResponseHandler;

/**
 * @since version
 */
public interface ConnectCapabilitiesClient
{
    RemoteApplicationWithCapabilities getCapabilities(String url);
    <T> T getCapabilitySet(RemoteApplicationWithCapabilities app, String key, ResponseHandler<T> responseHandler);
}
