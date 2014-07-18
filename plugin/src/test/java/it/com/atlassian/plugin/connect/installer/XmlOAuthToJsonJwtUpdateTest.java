package it.com.atlassian.plugin.connect.installer;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jwt.core.JwtUtil;
import com.atlassian.modzdetector.IOUtils;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.modules.beans.*;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.module.page.GeneralPageModuleDescriptor;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.util.zip.ZipBuilder;
import com.atlassian.plugin.connect.plugin.util.zip.ZipHandler;
import com.atlassian.plugin.connect.spi.Filenames;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.filter.AddonTestFilterResults;
import com.atlassian.plugin.connect.testsupport.filter.ServletRequestSnaphot;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.com.atlassian.plugin.connect.TestAuthenticator;
import it.com.atlassian.plugin.connect.util.RequestUtil;
import net.oauth.*;
import net.oauth.signature.OAuthSignatureMethod;
import net.oauth.signature.RSA_SHA1;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.*;

/**
 * Ensure that Connect supports add-ons updating from XML + OAuth to JSON + JWT.
 */
@XmlDescriptor
@RunWith(AtlassianPluginsTestRunner.class)
public class XmlOAuthToJsonJwtUpdateTest
{
    private static final Logger LOG = LoggerFactory.getLogger(XmlOAuthToJsonJwtUpdateTest.class);
    private static final String OLD_PLUGIN_KEY = "myaddon_helloworld";
    private static final String INSTALLED_URL_SUFFIX = "/installed";
    private static final String OLD_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n" +
            "                MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtWghUS3KRC01kJkeU83P\n" +
            "                S2CSH6Vma2twv7p7JVHUIWl69tNPvn46mYt3gbFQgmBv74pAJJkb1FvBNxhy0B19\n" +
            "                9fXgZUVS+6R7597t0hVG610Zbl4Ar7xvs/h3ACwUhSib3ad496nghLvOnarrJIgw\n" +
            "                sNQeCaBz+FczCYQAt7Mk8vGjE1XSha6FEBNKIYijgoZhqLGcTxxyBIDmMwyNU2Oz\n" +
            "                lknyFZ1ZhYIPKuw069njJjbuL0Nv9PXKO+pNZMQvwo7WrepJ0B0VG5KYjDzetFzx\n" +
            "                FOtr7x6M5Z7XZsPhNIjDjE+oBHu+Hzc1/bGyWpZrzc04nqCNTsmOvID1H50Jt778\n" +
            "                mQIDAQAB\n" +
            "                -----END PUBLIC KEY-----";
    private static final String OLD_PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEpQIBAAKCAQEAtWghUS3KRC01kJkeU83PS2CSH6Vma2twv7p7JVHUIWl69tNP\n" +
            "vn46mYt3gbFQgmBv74pAJJkb1FvBNxhy0B199fXgZUVS+6R7597t0hVG610Zbl4A\n" +
            "r7xvs/h3ACwUhSib3ad496nghLvOnarrJIgwsNQeCaBz+FczCYQAt7Mk8vGjE1XS\n" +
            "ha6FEBNKIYijgoZhqLGcTxxyBIDmMwyNU2OzlknyFZ1ZhYIPKuw069njJjbuL0Nv\n" +
            "9PXKO+pNZMQvwo7WrepJ0B0VG5KYjDzetFzxFOtr7x6M5Z7XZsPhNIjDjE+oBHu+\n" +
            "Hzc1/bGyWpZrzc04nqCNTsmOvID1H50Jt778mQIDAQABAoIBAQCLTi6fn1E/L5R9\n" +
            "uQfQBTEVylAMG0DeZsBLi5G7o+4Jxm2WE8meGGM5vB8GqjqQFCyBP6JoOGdlmRx0\n" +
            "CcNJTAyJj8pFGopSEgrQkaIBfTNb1L+NwIQ4b7U7+CayLCeJ5hhji5LaZUqzw2E0\n" +
            "NKekAy2Y7Rsv+1ZzM8tOmF7QsrJCGL8/WOq7QLVmuMKawOeGzp7H6RQ9BPjOBB/Q\n" +
            "swe2G5uzLY/SkTlVjEriVGuHP1NzX2t7QzV1HpLtVhLrCBKZvo1LJ9sfSXxOZmM3\n" +
            "/uCNE3+JMaTsMk0DSdZnY4pwfOPOfbUAf0TBHAKIu2styybPS46RPGRPDbxhkCTP\n" +
            "Wc1P7kCxAoGBAOl3I9n8NXwKfG30Z9KENgTXHYPvVdJKhcpRiqklq7KKv6oqmp4Z\n" +
            "0/A44whbaWl4P0ugmYsRifDJpVNE0C0Mw0nzWpuq84y6/GeU/VP+M+B17/yDMElj\n" +
            "3EXITNM2XrZlD6iYI6WleaNasKW5M1pVOhGiHdVTQxTRWm+eZYfiYmLtAoGBAMbq\n" +
            "ownQ1mDogKZUol5zq09z/6NeXFZpVhDNiaQzuX2J8wBh9Bkb9njtNPyg5+XoCw/c\n" +
            "0/UsplTSCEKxDWFHTOKCAEiqA7HQfHbqpykwX76DNnilCIKa1AXhVeUmu/+YYaOO\n" +
            "SAuyHOZlbKJaK8mjk4EEe1Mr39z537elsh1JDS7dAoGBAOQFqi14yKA6+abG5DRX\n" +
            "Tw9RLxGyS4cVpDCjjaOBGH5MR8Ci1dr+7OIeHZgG+CC8Ak4SMIUEf05/FAsNFao6\n" +
            "Ye6zUVbjE/bqliVw/i/wAqkDZ36gfyPe9b/uTyKnYsAQWsfWuFJMGU6z//4MsZxT\n" +
            "y2B3j13QcZ8+jm6gLRgXwvJNAoGATEF/JzAsPxJi32DqrhLhxZ/OjK6L74SKPf7N\n" +
            "mWlK3tmXkrn6ffW+UzV8bqywue5u7zHU/9SSH0o1aHu/iV9wFhWITlL+/5fRXzUt\n" +
            "yBiHW92pcC60SH1acraj2ykyQRYFuFG/RNyPP7P6JXMz/iT7UyaIsKXNOEWCgkC/\n" +
            "O4LZzvECgYEA4HaAE2ne4Wjevnsz8tQm97Of5XImPNI1EcIPH8shpsxB88ubw0hr\n" +
            "KUXwyj0dMvq7mB+8nJL9Hg/QWcqJ+buLRPcoqqaveRYXrVSnT+9MnYQ5LzqRdH3b\n" +
            "QBUnpzF6+17WlvPbE4t/OD0wzP6mUPh8UOgsU2bb+cVv4cMbroujLq8=\n" +
            "-----END RSA PRIVATE KEY-----";

    private static final String JWT_VERSION = "jwt-version";
    private static final String JWT_VERSION_SLASHED = "/" + JWT_VERSION;
    private static final String OAUTH_VERSION = "oauth-version";
    private static final String OAUTH_VERSION_SLASHED = "/" + OAUTH_VERSION;

    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final ConnectAddonRegistry connectAddonRegistry;
    private final AddonTestFilterResults testFilterResults;
    private final ConnectApplinkManager connectApplinkManager;
    private final RequestUtil requestUtil;

    private Plugin oAuthPlugin;
    private Plugin jwtPlugin;

    public XmlOAuthToJsonJwtUpdateTest(TestPluginInstaller testPluginInstaller,
                                       TestAuthenticator testAuthenticator,
                                       ConnectAddonRegistry connectAddonRegistry,
                                       AddonTestFilterResults testFilterResults,
                                       ConnectApplinkManager connectApplinkManager,
                                       ApplicationProperties applicationProperties)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.connectAddonRegistry = connectAddonRegistry;
        this.testFilterResults = testFilterResults;
        this.connectApplinkManager = connectApplinkManager;
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

        jwtPlugin = testPluginInstaller.installAddon(createJwtAddOn(oAuthPlugin));
        assertNotNull(jwtPlugin);
        oAuthPlugin = null; // we get to this line of code only if installing the update works
    }

    @AfterClass
    public void tearDown()
    {
        if (oAuthPlugin != null)
        {
            try
            {
                testPluginInstaller.uninstallXmlAddon(oAuthPlugin);
            }
            catch (IOException e)
            {
                LOG.error("Failed to uninstall test plugin " + oAuthPlugin.getKey() + " during teardown.", e);
            }
        }
        if (jwtPlugin != null)
        {
            try
            {
                testPluginInstaller.uninstallJsonAddon(jwtPlugin);
            }
            catch (IOException e)
            {
                LOG.error("Failed to uninstall test plugin " + jwtPlugin.getKey() + " during teardown.", e);
            }
        }
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

    private RequestUtil.Request constructOAuthRequestFromAddOn() throws IOException, OAuthException, URISyntaxException
    {
        final HttpMethod httpMethod = HttpMethod.GET;
        URI uri = URI.create(requestUtil.getApplicationRestUrl("/applinks/1.0/manifest"));
        uri = signOAuthUri(httpMethod, uri);

        return requestUtil.requestBuilder()
                .setMethod(httpMethod)
                .setUri(uri)
                .build();
    }

    private URI signOAuthUri(HttpMethod httpMethod, URI uri) throws IOException, OAuthException, URISyntaxException
    {
        final Map<String, String> oAuthParams = new HashMap<String, String>();
        {
            oAuthParams.put(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);
            oAuthParams.put(OAuth.OAUTH_VERSION, "1.0");
            oAuthParams.put(OAuth.OAUTH_CONSUMER_KEY, oAuthPlugin.getKey());
            oAuthParams.put(OAuth.OAUTH_NONCE, String.valueOf(System.nanoTime()));
            oAuthParams.put(OAuth.OAUTH_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000));
        }
        final OAuthMessage oAuthMessage = new OAuthMessage(httpMethod.toString(), uri.toString(), oAuthParams.entrySet());
        final OAuthConsumer oAuthConsumer = new OAuthConsumer(null, oAuthPlugin.getKey(), OLD_PRIVATE_KEY, new OAuthServiceProvider(null, null, null));
        oAuthConsumer.setProperty(RSA_SHA1.PRIVATE_KEY, OLD_PRIVATE_KEY);
        final OAuthSignatureMethod oAuthSignatureMethod = OAuthSignatureMethod.newSigner(oAuthMessage, new OAuthAccessor(oAuthConsumer));
        oAuthSignatureMethod.sign(oAuthMessage);
        return addOAuthParamsToRequest(uri, oAuthMessage);
    }

    private static URI addOAuthParamsToRequest(URI uri, OAuthMessage oAuthMessage) throws IOException
    {
        StringBuilder sb = new StringBuilder("?");
        {
            boolean isFirst = true;

            for (Map.Entry<String, String> entry : oAuthMessage.getParameters())
            {
                if (!isFirst)
                {
                    sb.append('&');
                }

                isFirst = false;
                sb.append(entry.getKey()).append('=').append(JwtUtil.percentEncode(entry.getValue())); // for JWT use the same encoding as OAuth 1
            }

            uri = URI.create(uri + sb.toString());
        }
        return uri;
    }
}
