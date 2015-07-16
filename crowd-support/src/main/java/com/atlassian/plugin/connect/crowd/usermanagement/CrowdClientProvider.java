package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.service.client.CrowdClient;

/**
 * An injectable means of working with the crowd clients and properties useful to Connect
 */
public interface CrowdClientProvider
{
    /**
     * Get a client able to communicate with the configured RemoteCrowd Server
     *
     * @return a client for the RemoteCrowd Server
     */
    CrowdClient getCrowdClient();
}
