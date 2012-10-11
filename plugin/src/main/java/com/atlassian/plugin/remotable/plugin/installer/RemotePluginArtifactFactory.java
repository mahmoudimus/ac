package com.atlassian.plugin.remotable.plugin.installer;

import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.remotable.spi.permission.PermissionsReader;
import com.atlassian.plugin.remotable.plugin.util.zip.ZipBuilder;
import com.atlassian.plugin.remotable.plugin.util.zip.ZipHandler;
import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.plugin.PluginArtifact;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;
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
    private final PermissionsReader permissionsReader;

    @Autowired
    public RemotePluginArtifactFactory(PermissionsReader permissionsReader)
    {
        this.permissionsReader = permissionsReader;
        this.classesToIncludeClass = extractClassesToIncludeClass();
    }

    public PluginArtifact create(URI registrationUrl, final Document document, String username
    )
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

    public PluginArtifact create(URI registrationUrl,
                                 JarPluginArtifact originalArtifact,
                                 final Document document,
                                 String username
    )
    {
        String pluginKey = document.getRootElement().attributeValue("key");
        changeDescriptorToIncludeRemotePluginHeader(registrationUrl, document, username);

        // todo: support local installations.  Will tackle when integrate with the upm
        Set<String> declaredPermissions = permissionsReader.readPermissionsFromDescriptor(document, InstallationMode.REMOTE);

        // todo: remove(?) class files and other things not allowed in remote mode

        // todo: scan plugin to ensure they asked for the right permissions


        throw new UnsupportedOperationException("This option is not yet available");
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
            in = getClass().getResourceAsStream("/remotable-plugins-kit-common.jar");
            ZipInputStream zin = new ZipInputStream(in);
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null)
            {
                if (CLASSES_TO_INCLUDE_CLASS_PATH.equals(entry.getName()))
                {
                    return IOUtils.toByteArray(zin);
                }
            }
            throw new IllegalStateException("Couldn't find com.atlassian.plugin.remotable.kit.common.ClassesToInclude");
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Couldn't read from remotable-plugins-kit-common.jar", e);
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }

    }
}
