package com.atlassian.plugin.connect.core.module;

import java.net.URI;

/**
 * Access to host information for the iframe being rendered
 */
public interface HostApplicationInfo
{
    /**
     * Gets the host URL, as seen by the 'user'
     * @return the host URL
     */
    URI getUrl();

    /**
     * The context path of the host application
     * @return the context path
     */
    String getContextPath();
}
