package it.com.atlassian.plugin.connect.installer;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.modzdetector.IOUtils;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.*;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.module.page.GeneralPageModuleDescriptor;
import com.atlassian.plugin.connect.plugin.util.zip.ZipBuilder;
import com.atlassian.plugin.connect.plugin.util.zip.ZipHandler;
import com.atlassian.plugin.connect.spi.Filenames;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugin.connect.testsupport.filter.ServletRequestSnaphot;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.com.atlassian.plugin.connect.TestAuthenticator;
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
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.*;

/**
 * Ensure that Connect supports add-ons updating from XML + OAuth to JSON + JWT.
 */
@RunWith(AtlassianPluginsTestRunner.class)
public class XmlOAuthToJsonJwtUpdateTest
{
    private static final Logger LOG = LoggerFactory.getLogger(XmlOAuthToJsonJwtUpdateTest.class);
    private static final String OLD_PLUGIN_KEY = "myaddon_helloworld";
    private static final String INSTALLED_URL_SUFFIX = "/installed";
    private static final String OLD_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n" +
            "                MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCuNwJVGkY9XWtuNe7p8PMOEr8O\n" +
            "                WetSAqMxWldFfmNYTbRsI/8ZX/S/5gm4UKZyFUDOICtVddYv1tWW/P31OA5khyQT\n" +
            "                XLp8sYpyNDBuwg00kfmBGleBgcKvePxMAr2y4La1OBz4aE+xK1HJojl2ToAubVY+\n" +
            "                qikVwxXolycVkz8AzQIDAQAB\n" +
            "                -----END PUBLIC KEY-----";

    private static final String JWT_VERSION = "jwt-version";
    private static final String JWT_VERSION_SLASHED = "/" + JWT_VERSION;
    private static final String OAUTH_VERSION = "oauth-version";
    private static final String OAUTH_VERSION_SLASHED = "/" + OAUTH_VERSION;

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final ConnectAddonRegistry connectAddonRegistry;
    private final AddonTestFilterResults testFilterResults;
    private final ConnectApplinkManager connectApplinkManager;

    private Plugin oAuthPlugin;
    private Plugin jwtPlugin;

    public XmlOAuthToJsonJwtUpdateTest(TestPluginInstaller testPluginInstaller,
                                       TestAuthenticator testAuthenticator,
                                       ConnectAddonRegistry connectAddonRegistry,
                                       AddonTestFilterResults testFilterResults,
                                       ConnectApplinkManager connectApplinkManager)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.connectAddonRegistry = connectAddonRegistry;
        this.testFilterResults = testFilterResults;
        this.connectApplinkManager = connectApplinkManager;
    }

    @BeforeClass
    public void beforeAllTests() throws IOException, URISyntaxException
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
        }

        jwtPlugin = testPluginInstaller.installAddon(createJwtAddOn(oAuthPlugin));
        assertNotNull(jwtPlugin);
        oAuthPlugin = null; // we get to this line of code only if installing the update works
    }

    @AfterClass
    public void afterAllTests()
    {
        uninstall(oAuthPlugin);
        uninstall(jwtPlugin);
    }

    @Test
    public void pluginKeyRemainsTheSame()
    {
        assertEquals(OLD_PLUGIN_KEY, jwtPlugin.getKey());
    }

    @Test
    public void baseUrlChanges()
    {
        assertFalse(getOldBaseUrl().equals(getNewBaseUrlFromRegistry()));
    }

    @Test
    public void newBaseUrlIsCorrect()
    {
        assertEquals(getOldBaseUrl().replace(OAUTH_VERSION, JWT_VERSION), getNewBaseUrlFromRegistry());
    }

    @Test
    public void baseUrlInRegistryAndInDescriptorAgree()
    {
        assertEquals(getNewBaseUrlFromRegistry(), getNewDescriptor().get("baseUrl").getAsString());
    }

    @Test
    public void authenticationMethodChanges()
    {
        assertEquals(AuthenticationType.JWT.toString().toLowerCase(), getDescriptor().get("authentication").getAsJsonObject().get("type").getAsString());
    }

    @Test
    public void publicKeyDisappears()
    {
        assertNull(getDescriptor().get("authentication").getAsJsonObject().get("publicKey"));
    }

    @Test
    public void sharedSecretIsNotOldPublicKey()
    {
        assertFalse(OLD_PUBLIC_KEY.equals(connectAddonRegistry.getSecret(jwtPlugin.getKey())));
    }

    @Test
    public void installedCallbackContainsCorrectKey()
    {
        assertEquals(OLD_PLUGIN_KEY, getLastInstallPayload().get("key").getAsString());
    }

    @Test
    public void installedCallbackContainsSharedSecret()
    {
        assertNotNull(getLastInstallPayload().get("sharedSecret").getAsString());
    }

    @Test
    public void installedCallbackContainsSameSharedSecretAsRegistry()
    {
        assertEquals(connectAddonRegistry.getSecret(jwtPlugin.getKey()), getLastInstallPayload().get("sharedSecret").getAsString());
    }

    @Test
    public void generalPageHasNewUrl()
    {
        assertEquals("/v2/helloworld.html", getDescriptor().get("modules").getAsJsonObject().get("generalPages").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString());
    }

    private JsonObject getLastInstallPayload()
    {
        ServletRequestSnaphot installRequest = testFilterResults.getRequest(jwtPlugin.getKey(), JWT_VERSION_SLASHED);
        return new JsonParser().parse(installRequest.getEntity()).getAsJsonObject();
    }

    private JsonObject getNewDescriptor()
    {
        return new JsonParser().parse(connectAddonRegistry.getDescriptor(jwtPlugin.getKey())).getAsJsonObject();
    }

    private String getNewBaseUrlFromRegistry()
    {
        return connectAddonRegistry.getBaseUrl(jwtPlugin.getKey());
    }

    private JsonObject getDescriptor()
    {
        return new JsonParser().parse(connectAddonRegistry.getDescriptor(jwtPlugin.getKey())).getAsJsonObject();
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
        String xml = IOUtils.toString(XmlOAuthToJsonJwtUpdateTest.class.getResourceAsStream("/com/atlassian/connect/xml_oauth_descriptor.xml"))
                .replace("{{localBaseUrl}}", oldBaseUrl)
                .replace("{{user}}", "admin")
                .replace("{{currentTimeMillis}}", String.valueOf(System.currentTimeMillis()));

        // preconditions
        {
            String displayUrlText = String.format("display-url=\"%s\"", oldBaseUrl);
            assertTrue(String.format("%s should contain %s", xml, displayUrlText), xml.indexOf(displayUrlText) > 0);

            String publicKeyText = String.format("<public-key>%s</public-key>", OLD_PUBLIC_KEY);
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

    private void uninstall(Plugin plugin)
    {
        if (null != plugin)
        {
            try
            {
                testPluginInstaller.uninstallPlugin(plugin);
            }
            catch (IOException e)
            {
                LOG.error("Failed to uninstall test plugin " + plugin.getKey() + " during teardown.", e);
            }
        }
    }

    private ConnectAddonBean createJwtAddOn(Plugin oldPlugin)
    {
        return new ConnectAddonBeanBuilder()
                .withKey(oldPlugin.getKey())
                .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(oldPlugin.getKey() + JWT_VERSION_SLASHED))
                .withAuthentication(AuthenticationBean.newAuthenticationBean()
                        .withType(AuthenticationType.JWT)
                        .build())
                .withLifecycle(LifecycleBean.newLifecycleBean()
                        .withInstalled(INSTALLED_URL_SUFFIX)
                        .build())
                .withModule("generalPages", ConnectPageModuleBean.newPageBean()
                    .withUrl("/v2/helloworld.html")
                    .withKey("general")
                    .withName(new I18nProperty("Greeting", "greeting"))
                    .build())
                .build();
    }
}
