package com.atlassian.plugin.connect.test.plugin.service;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.service.LegacyAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.xmldescriptor.XmlDescriptorExploderUnitTestHelper;
import com.atlassian.plugin.connect.spi.ConnectAddOnIdentifierService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;

import java.io.ByteArrayInputStream;
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

    @BeforeClass
    public static void beforeAnyTest()
    {
        XmlDescriptorExploderUnitTestHelper.runBeforeTests();
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

}
