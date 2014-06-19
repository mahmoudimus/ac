package com.atlassian.plugin.connect.plugin.installer;

import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.util.zip.ZipBuilder;
import com.atlassian.plugin.connect.plugin.util.zip.ZipHandler;
import com.atlassian.plugin.connect.plugin.xmldescriptor.XmlDescriptorExploder;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.spi.Filenames;
import org.dom4j.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Creates plugin artifacts for plugins installed in remote mode
 *
 * @deprecated we no longer create artifacts for json descriptors. use {@link ConnectAddonToPluginFactory} instead.
 */
@Deprecated
@Component
@XmlDescriptor
public class RemotePluginArtifactFactory
{
    public PluginArtifact create(final Document document, String username)
    {
        String pluginKey = document.getRootElement().attributeValue("key");

        XmlDescriptorExploder.notifyAndExplode(pluginKey);

        changeDescriptorToIncludeRemotePluginHeader(document, username);

        return new JarPluginArtifact(ZipBuilder.buildZip("install-" + pluginKey, new ZipHandler()
        {
            @Override
            public void build(ZipBuilder builder) throws IOException
            {
                builder.addFile(Filenames.ATLASSIAN_PLUGIN_XML, document);
            }
        }));
    }

    private void changeDescriptorToIncludeRemotePluginHeader(Document document, String username)
    {
        // fixme: plugin osgi manifest generator should respect existing entries, but it currently just blows everything away,
        // so we have to store the values in the plugin descriptor instead
        document.getRootElement()
                .element("plugin-info")
                .addElement("bundle-instructions")
                .addElement(ConnectAddOnIdentifierService.REMOTE_PLUGIN)
                .addText("installer;user=\"" + username + "\";date=\"" + System.currentTimeMillis() + "\"");
    }

}
