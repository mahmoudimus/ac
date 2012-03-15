package com.atlassian.labs.remoteapps.api;

import org.dom4j.Document;

/**
 * Kicks off the loading of the remote app, ideally before the Spring context
 * is finished loading.
 */
public interface DescriptorGenerator
{
    void init(Document descriptor) throws Exception;
}
