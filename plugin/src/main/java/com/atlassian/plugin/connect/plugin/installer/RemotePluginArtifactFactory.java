package com.atlassian.plugin.connect.plugin.installer;

import java.io.IOException;
import java.net.URI;

import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.connect.plugin.util.zip.ZipBuilder;
import com.atlassian.plugin.connect.plugin.util.zip.ZipHandler;

import org.dom4j.Document;
import org.springframework.stereotype.Component;

/**
 * Creates plugin artifacts for plugins installed in remote mode
 */
@Component
public class RemotePluginArtifactFactory
{
    public PluginArtifact create(URI registrationUrl, final Document document, String username)
    {
        String pluginKey = document.getRootElement().attributeValue("key");
        changeDescriptorToIncludeRemotePluginHeader(document, registrationUrl, username);

        return new JarPluginArtifact(ZipBuilder.buildZip("install-" + pluginKey, new ZipHandler()
        {
            @Override
            public void build(ZipBuilder builder) throws IOException
            {
                builder.addFile("atlassian-plugin.xml", document);
            }
        }));
    }

    private void changeDescriptorToIncludeRemotePluginHeader(Document document, URI registrationUrl, String username)
    {
        // fixme: plugin osgi manifest generator should respect existing entries, but it currently just blows everything away,
        // so we have to store the values in the plugin descriptor instead
        document.getRootElement()
                .element("plugin-info")
                .addElement("bundle-instructions")
                .addElement("Remote-Plugin")
                .addText("installer;user=\"" + username + "\";date=\"" + System.currentTimeMillis() + "\"" +
                        ";registration-url=\"" + registrationUrl + "\"");
    }
}
