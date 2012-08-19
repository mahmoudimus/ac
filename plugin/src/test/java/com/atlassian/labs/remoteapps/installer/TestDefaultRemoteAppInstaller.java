package com.atlassian.labs.remoteapps.installer;

import com.atlassian.labs.remoteapps.descriptor.DescriptorValidator;
import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.labs.remoteapps.api.FormatConverter;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.net.RequestFactory;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.jar.Manifest;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.parseDocument;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.readDocument;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TestDefaultRemoteAppInstaller
{

    private DefaultRemoteAppInstaller installer;

    @Before
    public void setUp() throws Exception
    {
        installer = new DefaultRemoteAppInstaller(mock(ConsumerService.class),
                mock(RequestFactory.class), mock(PluginController.class), mock(ApplicationProperties.class),
                mock(DescriptorValidator.class), mock(PluginAccessor.class), mock(OAuthLinkManager.class),
                mock(FormatConverter.class), mock(InstallerHelper.class), mock(BundleContext.class));
    }

    @Test
    public void testConvertPluginDescriptorIntoJar() throws IOException
    {
        Document descriptor = DocumentFactory.getInstance().createDocument()
                .addElement("atlassian-plugin")
                    .addElement("plugin-info")
                    .getDocument();
        JarPluginArtifact artifact = installer.convertPluginDescriptorIntoJar(
                "foo", descriptor, "bob", URI.create("http://localhost")
        );

        Document newDoc = readDocument(artifact.getResourceAsStream("atlassian-plugin.xml"));
        Element bundleInst = newDoc.getRootElement().element("plugin-info").element("bundle-instructions");
        Map<String,Map<String,String>> attrs = OsgiHeaderUtil.parseHeader(bundleInst.element("Remote-Plugin").getTextTrim());
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
