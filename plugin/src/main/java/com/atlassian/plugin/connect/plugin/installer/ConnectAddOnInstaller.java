package com.atlassian.plugin.connect.plugin.installer;

import java.net.URI;

import com.atlassian.plugin.Plugin;

import org.dom4j.Document;

/**
 * @since version
 */
public interface ConnectAddOnInstaller
{
    Plugin install(String username, Document document);

    Plugin install(String username, String capabilities);
}
