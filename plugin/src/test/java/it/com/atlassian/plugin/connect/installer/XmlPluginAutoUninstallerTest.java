package it.com.atlassian.plugin.connect.installer;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.modzdetector.IOUtils;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.integration.plugins.XmlPluginAutoUninstallHelper;
import com.atlassian.plugin.connect.plugin.module.page.GeneralPageModuleDescriptor;
import com.atlassian.plugin.connect.plugin.util.zip.ZipBuilder;
import com.atlassian.plugin.connect.plugin.util.zip.ZipHandler;
import com.atlassian.plugin.connect.spi.Filenames;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.TestConstants;
import it.com.atlassian.plugin.connect.util.RequestUtil;
import net.oauth.OAuthException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * test the installing of xml addons on start up. Note this auto uninstaller is invoked automatically as not practical
 * to restart the server mid test
 */
@XmlDescriptor
@RunWith(AtlassianPluginsTestRunner.class)
public class XmlPluginAutoUninstallerTest
{
    private static final Logger LOG = LoggerFactory.getLogger(XmlPluginAutoUninstallerTest.class);
    private static final String OLD_PLUGIN_KEY = "myaddon_helloworld";

    private static final String OAUTH_VERSION = "oauth-version";
    private static final String OAUTH_VERSION_SLASHED = "/" + OAUTH_VERSION;

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final ConnectApplinkManager connectApplinkManager;
    private final XmlPluginAutoUninstallHelper xmlPluginAutoUninstallHelper;
    private final PluginAccessor pluginAccessor;
    private final RequestUtil requestUtil;

    private Plugin oAuthPlugin;

    // This test is identical to XmlOAuthToJsonJwtUpdateTest but explicitly uninstalls the xml addon first.
    // This test the fix for ACDEV-1474
    public XmlPluginAutoUninstallerTest(TestPluginInstaller testPluginInstaller,
                                        TestAuthenticator testAuthenticator,
                                        ConnectApplinkManager connectApplinkManager,
                                        ApplicationProperties applicationProperties,
                                        XmlPluginAutoUninstallHelper xmlPluginAutoUninstallHelper,
                                        PluginAccessor pluginAccessor)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.connectApplinkManager = connectApplinkManager;
        this.xmlPluginAutoUninstallHelper = xmlPluginAutoUninstallHelper;
        this.pluginAccessor = pluginAccessor;
        this.requestUtil = new RequestUtil(applicationProperties);
    }

    @BeforeClass
    public void setUp() throws IOException, URISyntaxException, OAuthException
    {
        testAuthenticator.authenticateUser("admin");
        oAuthPlugin = testPluginInstaller.installPlugin(createXmlDescriptorFile());

        // preconditions
        {
            assertNotNull(oAuthPlugin);
            assertEquals(OLD_PLUGIN_KEY, oAuthPlugin.getKey());
            assertEquals(4, oAuthPlugin.getModuleDescriptors().size());
            ModuleDescriptor<?> moduleDescriptor = oAuthPlugin.getModuleDescriptor("general");
            assertEquals("Greeting", moduleDescriptor.getName());
            assertTrue(moduleDescriptor instanceof GeneralPageModuleDescriptor);
            ApplicationLink appLink = connectApplinkManager.getAppLink(oAuthPlugin.getKey());
            assertEquals(getOldBaseUrl(), appLink.getDisplayUrl().toString());

            // old xml add-on can send requests
            assertEquals("old xml add-on should be able to make requests", 200, requestUtil.makeRequest(constructOAuthRequestFromAddOn()).getStatusCode());
        }

        assertNotNull(pluginAccessor.getPlugin(oAuthPlugin.getKey()));
    }

    @AfterClass
    public void tearDown()
    {
        if (oAuthPlugin != null)
        {
            try
            {
                testPluginInstaller.uninstallXmlAddon(oAuthPlugin);
            } catch (IOException e)
            {
                LOG.error("Failed to uninstall test plugin " + oAuthPlugin.getKey() + " during teardown.", e);
            }
        }
    }

    @Test
    public void pluginWasRemoved()
    {
        // kicks off auto uninstaller
        xmlPluginAutoUninstallHelper.uninstallXmlPlugins();

        assertEquals(null, pluginAccessor.getPlugin(oAuthPlugin.getKey()));
    }

    private File createXmlDescriptorFile() throws IOException
    {
        File xmlFile = File.createTempFile(getClass().getSimpleName(), ".xml");
        xmlFile.deleteOnExit();
        return ZipBuilder.buildZip("install-" + OLD_PLUGIN_KEY, new ZipHandler()
        {
            @Override
            public void build(ZipBuilder builder) throws IOException
            {
                builder.addFile(Filenames.ATLASSIAN_PLUGIN_XML, getOldXmlDescriptorContent());
            }
        });
    }

    private String getOldXmlDescriptorContent() throws IOException
    {
        String oldBaseUrl = getOldBaseUrl();
        String xml = IOUtils.toString(XmlPluginAutoUninstallerTest.class.getResourceAsStream(TestConstants.XML_ADDON_RESOURCE_PATH))
                .replace("{{localBaseUrl}}", oldBaseUrl)
                .replace("{{user}}", "admin")
                .replace("{{currentTimeMillis}}", String.valueOf(System.currentTimeMillis()));

        // preconditions
        {
            String displayUrlText = String.format("display-url=\"%s\"", oldBaseUrl);
            assertTrue(String.format("%s should contain %s", xml, displayUrlText), xml.indexOf(displayUrlText) > 0);

            String publicKeyText = String.format("<public-key>%s</public-key>", TestConstants.XML_ADDON_PUBLIC_KEY);
            assertTrue(String.format("%s should contain %s", xml, publicKeyText), xml.indexOf(publicKeyText) > 0);

            String pluginKeyText = String.format("key=\"%s\"", OLD_PLUGIN_KEY);
            assertTrue(String.format("%s should contain %s", xml, pluginKeyText), xml.indexOf(pluginKeyText) > 0);
        }

        return xml;
    }

    private String getOldBaseUrl()
    {
        return testPluginInstaller.getInternalAddonBaseUrl(OLD_PLUGIN_KEY) + OAUTH_VERSION_SLASHED;
    }

    private RequestUtil.Request constructOAuthRequestFromAddOn() throws IOException, OAuthException, URISyntaxException
    {
        return requestUtil.constructOAuthRequestFromAddOn(oAuthPlugin.getKey());
    }
}
