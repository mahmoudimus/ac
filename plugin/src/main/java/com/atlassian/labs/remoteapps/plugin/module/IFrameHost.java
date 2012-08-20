package com.atlassian.labs.remoteapps.plugin.module;

/**
 * Access to host information for the iframe being rendered
 */
public interface IFrameHost
{
    /**
     * Gets the host URL, as seen by the 'user'
     * @return the host URL
     */
    String getUrl();

    /**
     * The context path of the host application
     * @return the context path
     */
    String getContextPath();
}
