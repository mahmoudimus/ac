package com.atlassian.labs.remoteapps.api;

import org.dom4j.Document;

import javax.servlet.http.HttpServlet;

/**
 * Kicks off the loading of the remote app, ideally before the Spring context
 * is finished loading.
 */
public interface DescriptorGenerator
{
    String getLocalMountBaseUrl();

    void init(Document descriptor) throws Exception;

    void mountServlet(HttpServlet httpServlet, String... urlPatterns);

    /**
     *
     * @param resourceBasePath Path in plugin that static files should be served from
     */
    void mountStaticResources(String resourceBasePath);
}
