package com.atlassian.labs.remoteapps.kit.servlet;

import org.dom4j.Document;

/**
 * Extension point to allow the app to load its own descriptor
 */
public interface RemoteAppDescriptorFactory
{
    Document create();
}
