package com.atlassian.plugin.remotable.plugin.installer;

import com.atlassian.plugin.remotable.host.common.descriptor.DescriptorPermissionsReader;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.readDocument;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 *
 */
public class TestRemotePluginArtifactFactory
{

    @Test
    public void testConvertPluginDescriptorIntoJar() throws IOException
    {
        RemotePluginArtifactFactory factory = new RemotePluginArtifactFactory(mock(DescriptorPermissionsReader.class));
        Document descriptor = DocumentFactory.getInstance()
                                             .createDocument()
                                             .addElement("atlassian-plugin")
                                             .addElement("plugin-info")
                                             .getDocument();
        PluginArtifact artifact = factory.create(URI.create("http://localhost"), descriptor, "bob");

        Document newDoc = readDocument(artifact.getResourceAsStream("atlassian-plugin.xml"));
        Element bundleInst = newDoc.getRootElement().element("plugin-info").element("bundle-instructions");
        Map<String, Map<String, String>> attrs = OsgiHeaderUtil.parseHeader(
                bundleInst.element("Remote-Plugin").getTextTrim());
        /*
Manifest mf = new Manifest(new ByteArrayInputStream(
        toByteArray(artifact.getResourceAsStream("META-INF/MANIFEST.MF"))));
Map<String,Map<String,String>> attrs = OsgiHeaderUtil.parseHeader(
        mf.getMainAttributes().getValue("Remote-Plugin"));
        */
        assertEquals("bob", attrs.get("installer").get("user"));
        assertEquals("http://localhost", attrs.get("installer").get("registration-url"));
    }
}
