package com.atlassian.plugin.connect.test.plugin.service;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.service.LegacyAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.xmldescriptor.XmlDescriptorExploderUnitTestHelper;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;
import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestConnectAddOnIdentifier
{
    private static final String CONNECT_MANIFEST = "Manifest-Version: 1.0\n" +
            "Bundle-Vendor: (unknown)\n" +
            "Private-Package: .\n" +
            "Bundle-ClassPath: .\n" +
            "Remote-Plugin: installer;user=\"admin\";date=\"1376503574345\";registratio\n" +
            " n-url=\"http://localhost:53066/register\"\n" +
            "Bundle-Version: 1\n" +
            "Tool: Bnd-1.43.0\n" +
            "Bundle-Name: 1msz5edwyfu0zeo2fs1e\n" +
            "Spring-Context: *;timeout:=60\n" +
            "Bnd-LastModified: 1376503574361\n" +
            "Created-By: 1.7.0_25 (Oracle Corporation)\n" +
            "Bundle-ManifestVersion: 2\n" +
            "Bundle-SymbolicName: 1msz5edwyfu0zeo2fs1e\n" +
            "Atlassian-Plugin-Key: 1msz5edwyfu0zeo2fs1e";

    private static final String LOCAL_MANIFEST = "Manifest-Version: 1.0\n" +
            "Bundle-Vendor: (unknown)\n" +
            "Private-Package: .\n" +
            "Bundle-ClassPath: .\n" +
            "Bundle-Version: 1\n" +
            "Tool: Bnd-1.43.0\n" +
            "Bundle-Name: 1msz5edwyfu0zeo2fs1e\n" +
            "Spring-Context: *;timeout:=60\n" +
            "Bnd-LastModified: 1376503574361\n" +
            "Created-By: 1.7.0_25 (Oracle Corporation)\n" +
            "Bundle-ManifestVersion: 2\n" +
            "Bundle-SymbolicName: 1msz5edwyfu0zeo2fs1e\n" +
            "Atlassian-Plugin-Key: 1msz5edwyfu0zeo2fs1e";

    private static final String CONNECT_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<atlassian-plugin key=\"1msz5edwyfu0zeo2fs1e\" name=\"1msz5edwyfu0zeo2fs1e\" plugins-version=\"2\"><plugin-info><version>1</version><permissions><permission>create_oauth_link</permission></permissions><bundle-instructions><Remote-Plugin>installer;user=\"admin\";date=\"1376503574345\";registration-url=\"http://localhost:53066/register\"</Remote-Plugin></bundle-instructions></plugin-info><remote-plugin-container key=\"container\" display-url=\"http://localhost:53066\"><oauth><public-key>-----BEGIN PUBLIC KEY-----\n" +
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAan9ZCQz5NUJEYQXIa+qVmhpj\n" +
            "sfNHpVj4zrfavBLYwZ2ocIE1ysyOKCKOaWgHpkB2DD3IeRfaS5wnjjRN2v5QT5Qt\n" +
            "/u24ZowIlJkGCHeJipFfPKPWKOErSBi0LiCqNhmvyVtiuFUU13T32e/1KLXNRujA\n" +
            "yC+ay018DC4bc6tPnQIDAQAB\n" +
            "-----END PUBLIC KEY-----\n" +
            "</public-key></oauth></remote-plugin-container></atlassian-plugin>";

    private static final String LOCAL_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<atlassian-plugin key=\"1msz5edwyfu0zeo2fs1e\" name=\"1msz5edwyfu0zeo2fs1e\" plugins-version=\"2\"><plugin-info><version>1</version><permissions><permission>create_oauth_link</permission></permissions></plugin-info></atlassian-plugin>";

    private File xml = null;

    @BeforeClass
    public static void beforeAnyTest()
    {
        XmlDescriptorExploderUnitTestHelper.runBeforeTests();
    }

    @After
    public void afterEachTest()
    {
        if (null != xml)
        {
            FileUtils.deleteQuietly(xml);
        }
    }

    @Test
    public void bundleWithHeaderReturnsTrue() throws Exception
    {
        Hashtable map = new Hashtable();
        map.put("Remote-Plugin", "");

        Bundle bundle = mock(Bundle.class);
        PluginAccessor accessor = mock(PluginAccessor.class);

        when(bundle.getHeaders()).thenReturn((Dictionary) map);

        ConnectAddOnIdentifierService ident = new LegacyAddOnIdentifierService(accessor);

        assertTrue(ident.isConnectAddOn(bundle));

    }

    @Test
    public void bundleWithoutHeaderReturnsFalse() throws Exception
    {
        Hashtable map = new Hashtable();

        Bundle bundle = mock(Bundle.class);
        PluginAccessor accessor = mock(PluginAccessor.class);

        when(bundle.getHeaders()).thenReturn((Dictionary) map);

        ConnectAddOnIdentifierService ident = new LegacyAddOnIdentifierService(accessor);

        assertFalse(ident.isConnectAddOn(bundle));

    }

    @Test
    public void pluginWithHeaderReturnsTrue() throws Exception
    {

        Plugin plugin = mock(Plugin.class);
        PluginAccessor accessor = mock(PluginAccessor.class);

        when(plugin.getResourceAsStream(anyString())).thenReturn(new ByteArrayInputStream(CONNECT_MANIFEST.getBytes()));

        ConnectAddOnIdentifierService ident = new LegacyAddOnIdentifierService(accessor);

        assertTrue(ident.isConnectAddOn(plugin));

    }

    @Test
    public void pluginWithoutHeaderReturnsFalse() throws Exception
    {
        Plugin plugin = mock(Plugin.class);
        PluginAccessor accessor = mock(PluginAccessor.class);

        when(plugin.getResourceAsStream(anyString())).thenReturn(new ByteArrayInputStream(LOCAL_MANIFEST.getBytes()));

        ConnectAddOnIdentifierService ident = new LegacyAddOnIdentifierService(accessor);

        assertFalse(ident.isConnectAddOn(plugin));

    }

    @Test
    public void pluginWithoutManifestReturnsFalse() throws Exception
    {
        Plugin plugin = mock(Plugin.class);
        PluginAccessor accessor = mock(PluginAccessor.class);

        when(plugin.getResourceAsStream(anyString())).thenReturn(null);

        ConnectAddOnIdentifierService ident = new LegacyAddOnIdentifierService(accessor);

        assertFalse(ident.isConnectAddOn(plugin));

    }

    @Test
    public void documentWithContainerReturnsTrue() throws Exception
    {
        PluginAccessor accessor = mock(PluginAccessor.class);
        Document doc = DocumentHelper.parseText(CONNECT_XML);

        ConnectAddOnIdentifierService ident = new LegacyAddOnIdentifierService(accessor);

        assertTrue(ident.isConnectAddOn(doc));
    }

    @Test
    public void documentWithoutContainerReturnsFalse() throws Exception
    {
        PluginAccessor accessor = mock(PluginAccessor.class);
        Document doc = DocumentHelper.parseText(LOCAL_XML);

        ConnectAddOnIdentifierService ident = new LegacyAddOnIdentifierService(accessor);

        assertFalse(ident.isConnectAddOn(doc));

    }

    @Test
    public void nullDocumentReturnsFalse() throws Exception
    {
        PluginAccessor accessor = mock(PluginAccessor.class);

        ConnectAddOnIdentifierService ident = new LegacyAddOnIdentifierService(accessor);

        assertFalse(ident.isConnectAddOn((Document) null));

    }

    @Test
    public void fileWithContainerReturnsTrue() throws Exception
    {
        PluginAccessor accessor = mock(PluginAccessor.class);

        xml = File.createTempFile("atlassian-plugin", ".xml");
        FileUtils.writeStringToFile(xml, CONNECT_XML);

        ConnectAddOnIdentifierService ident = new LegacyAddOnIdentifierService(accessor);

        assertTrue(ident.isConnectAddOn(xml));
    }

    @Test
    public void fileWithoutContainerReturnsFalse() throws Exception
    {
        PluginAccessor accessor = mock(PluginAccessor.class);

        xml = File.createTempFile("atlassian-plugin", ".xml");
        FileUtils.writeStringToFile(xml, LOCAL_XML);

        ConnectAddOnIdentifierService ident = new LegacyAddOnIdentifierService(accessor);

        assertFalse(ident.isConnectAddOn(xml));
    }

    @Test
    public void nonXmlFileReturnsFalse() throws Exception
    {
        PluginAccessor accessor = mock(PluginAccessor.class);

        xml = File.createTempFile("atlassian-plugin", ".xml");
        FileUtils.writeStringToFile(xml, "hi mom!");

        ConnectAddOnIdentifierService ident = new LegacyAddOnIdentifierService(accessor);

        assertFalse(ident.isConnectAddOn(xml));
    }

    @Test
    public void nullFileReturnsFalse() throws Exception
    {
        PluginAccessor accessor = mock(PluginAccessor.class);

        ConnectAddOnIdentifierService ident = new LegacyAddOnIdentifierService(accessor);

        assertFalse(ident.isConnectAddOn((File) null));

    }

}
