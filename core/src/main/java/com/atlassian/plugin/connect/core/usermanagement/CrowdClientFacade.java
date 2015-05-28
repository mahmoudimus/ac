package com.atlassian.plugin.connect.core.usermanagement;

import com.atlassian.crowd.service.client.CrowdClient;

/**
 * An injectable means of working with the crowd clients and properties useful to Connect
 */
public interface CrowdClientFacade
{
    /**
     * Get a client able to communicate with the configured RemoteCrowd Server
     *
     * @return a client for the RemoteCrowd Server
     */
    CrowdClient getCrowdClient();

    /**
     * Get the crowd client's name for the host application (i.e. "jira" or "confluence")
     *
     * @return the crowd client's name for the host application (i.e. "jira" or "confluence")
     */
    String getClientApplicationName();
}
