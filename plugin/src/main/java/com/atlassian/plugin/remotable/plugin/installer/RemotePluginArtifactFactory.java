package com.atlassian.plugin.remotable.plugin.installer;

import com.atlassian.plugin.remotable.plugin.util.zip.ZipBuilder;
import com.atlassian.plugin.remotable.plugin.util.zip.ZipHandler;
import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.plugin.PluginArtifact;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Creates plugin artifacts for plugins installed in remote mode
 */
@Component
public class RemotePluginArtifactFactory
{
    public static final String CLASSES_TO_INCLUDE_CLASS_PATH = "com/atlassian/plugin/remotable/kit/common/ClassesToInclude.class";

    private final byte[] classesToIncludeClass;

    public RemotePluginArtifactFactory()
    {
        this.classesToIncludeClass = extractClassesToIncludeClass();
    }

    public PluginArtifact create(URI registrationUrl, final Document document, String username)
    {
        String pluginKey = document.getRootElement().attributeValue("key");
        changeDescriptorToIncludeRemotePluginHeader(registrationUrl, document, username);


        return new JarPluginArtifact(ZipBuilder.buildZip("install-" + pluginKey, new ZipHandler()
        {
            @Override
            public void build(ZipBuilder builder) throws IOException
            {
                builder.addFile("atlassian-plugin.xml", document);
                builder.addFile(CLASSES_TO_INCLUDE_CLASS_PATH, new ByteArrayInputStream(classesToIncludeClass));
            }
        }));
    }

    private void changeDescriptorToIncludeRemotePluginHeader(URI registrationUrl, Document document, String username)
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

    private byte[] extractClassesToIncludeClass()
    {
        InputStream in = null;
        try
        {
            return IOUtils.toByteArray(
                    getClass().getResourceAsStream("/" + ClassesToInclude.class.getName().replace('.', '/') + ".class"));
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Couldn't read from classes to include class", e);
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }

    }
}
