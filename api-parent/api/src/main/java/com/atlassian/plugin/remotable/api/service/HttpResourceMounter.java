package com.atlassian.plugin.remotable.api.service;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;

/**
 * Allows a universal binary to mount HTTP resources for local or remote consumption
 */
public interface HttpResourceMounter
{
    String getLocalMountBaseUrl();

    /**
     * Filters are only processed right before a servlet and cannot be inserted anywhere in the chain.  They'll be
     * invoked on all dispatcher types.
     */
    void mountFilter(Filter filter, String... urlPatterns);

    void mountServlet(HttpServlet httpServlet, String... urlPatterns);

    /**
     *
     * @param resourceBasePath Path in plugin that static files should be served from
     */
    void mountStaticResources(String resourceBasePath, String urlPattern);
}
