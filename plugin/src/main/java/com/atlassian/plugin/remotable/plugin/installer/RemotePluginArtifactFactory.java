package com.atlassian.plugin.remotable.plugin.installer;

import com.atlassian.plugin.remotable.plugin.util.zip.ZipBuilder;
import com.atlassian.plugin.remotable.plugin.util.zip.ZipHandler;
import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.plugin.PluginArtifact;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static java.lang.String.format;

/**
 * Creates plugin artifacts for plugins installed in remote mode
 */
@Component
public class RemotePluginArtifactFactory
{
    private final byte[] classesToIncludeClass;

    public RemotePluginArtifactFactory()
    {
        this.classesToIncludeClass = extractClassesToIncludeClass();
    }

    public PluginArtifact create(URI registrationUrl, final Document document, String username)
    {
        String pluginKey = document.getRootElement().attributeValue("key");
        addExecuteJavaPermission(document);
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

    @VisibleForTesting
    Document addExecuteJavaPermission(Document document)
    {
        final Element permissions = getPermissionsElement(document);
        final Node permission = permissions.selectSingleNode(format("permission[text()='%s' and (not(@installation-mode or @installation-mode = 'remote')]", "execute_java"));
        if (permission == null)
        {
            permissions.addElement("permission").addAttribute("installation-mode", "remote").setText("execute_java");
        }
        return document;
    }

    private Element getPermissionsElement(Document document)
    {
        final Element permissions = document.getRootElement().element("plugin-info").element("permissions");
        if (permissions != null)
        {
            return permissions;
        }
        else
        {
            return document.getRootElement().element("plugin-info").addElement("permissions");
        }
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
