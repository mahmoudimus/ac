package com.atlassian.plugin.remotable.plugin.installer;

import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.readDocument;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public final class TestRemotePluginArtifactFactory
{
    private RemotePluginArtifactFactory remotePluginArtifactFactory;

    @Before
    public void setUp()
    {
        remotePluginArtifactFactory = new RemotePluginArtifactFactory();
    }

    @Test
    public void testConvertPluginDescriptorIntoJar() throws IOException
    {
        PluginArtifact artifact = remotePluginArtifactFactory.create(URI.create("http://localhost"), getAtlassianPluginXml(), "bob");

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

    @Test
    public void testAddExecuteJavaPermissionForAtlassianPluginXmlWithNoPermissions()
    {
        Document document = remotePluginArtifactFactory.addExecuteJavaPermission(getAtlassianPluginXml());

        asserSingleNodeExists(document, "/atlassian-plugin/plugin-info/permissions/permission[text() = 'execute_java' and @installation-mode='remote']");
    }

    @Test
    public void testAddExecuteJavaPermissionForAtlassianPluginXmlWithExecuteJavaPermissionAndNoInstallMode()
    {
        Document atlassianPluginXml = getAtlassianPluginXml();
        atlassianPluginXml.getRootElement()
                .element("plugin-info")
                .addElement("permissions")
                .addElement("permission").setText("execute_java");

        Document document = remotePluginArtifactFactory.addExecuteJavaPermission(atlassianPluginXml);
        asserSingleNodeExists(document, "/atlassian-plugin/plugin-info/permissions/permission[text() = 'execute_java' and not(@installation-mode)]");
    }

    @Test
    public void testAddExecuteJavaPermissionForAtlassianPluginXmlWithExecuteJavaPermissionAndInstallModeLocal()
    {
        Document atlassianPluginXml = getAtlassianPluginXml();
        atlassianPluginXml.getRootElement()
                .element("plugin-info")
                .addElement("permissions")
                .addElement("permission").addAttribute("installation-mode", "local").setText("execute_java");

        Document document = remotePluginArtifactFactory.addExecuteJavaPermission(atlassianPluginXml);

        asserSingleNodeExists(document, "/atlassian-plugin/plugin-info/permissions/permission[text() = 'execute_java' and @installation-mode = 'local']");
        asserSingleNodeExists(document, "/atlassian-plugin/plugin-info/permissions/permission[text() = 'execute_java' and @installation-mode = 'remote']");
    }

    @Test
    public void testAddExecuteJavaPermissionForAtlassianPluginXmlWithExecuteJavaPermissionAndInstallModeRemote()
    {
        Document atlassianPluginXml = getAtlassianPluginXml();
        atlassianPluginXml.getRootElement()
                .element("plugin-info")
                .addElement("permissions")
                .addElement("permission").addAttribute("installation-mode", "remote").setText("execute_java");

        Document document = remotePluginArtifactFactory.addExecuteJavaPermission(atlassianPluginXml);

        asserSingleNodeExists(document, "/atlassian-plugin/plugin-info/permissions/permission[text() = 'execute_java' and @installation-mode = 'remote']");
    }

    private void asserSingleNodeExists(Document document, String xpath)
    {
        assertNotNull("Could not find expected permission in plugin descriptor:\n" + document.asXML(),
                document.selectSingleNode(xpath));
    }

    private Document getAtlassianPluginXml()
    {
        return DocumentFactory.getInstance()
                .createDocument()
                .addElement("atlassian-plugin")
                .addElement("plugin-info")
                .getDocument();
    }
}
