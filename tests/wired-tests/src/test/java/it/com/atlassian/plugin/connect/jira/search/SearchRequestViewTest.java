package it.com.atlassian.plugin.connect.jira.search;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.google.common.base.Strings;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean.newSearchRequestViewModuleBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(AtlassianPluginsTestRunner.class)
public class SearchRequestViewTest
{
    private final TestPluginInstaller testPluginInstaller;
    private final TestAuthenticator testAuthenticator;
    private final ConnectAddonRegistry connectAddonRegistry;

    public SearchRequestViewTest(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator, ConnectAddonRegistry connectAddonRegistry)
    {
        this.testPluginInstaller = testPluginInstaller;
        this.testAuthenticator = testAuthenticator;
        this.connectAddonRegistry = connectAddonRegistry;
    }

    @Test
    public void canInstallWithQuoteInUrl() throws IOException
    {
        final String key = getClass().getSimpleName() + '-' + System.currentTimeMillis();
        final String url = "/page\"";
        Plugin addon = testPluginInstaller.installAddon(newConnectAddonBean()
                        .withKey(key)
                        .withBaseurl(testPluginInstaller.getInternalAddonBaseUrl(key))
                        .withAuthentication(newAuthenticationBean()
                                .withType(AuthenticationType.NONE)
                                .build())
                        .withModule("jiraSearchRequestViews", newSearchRequestViewModuleBean()
                                .withKey("page")
                                .withName(new I18nProperty("Hello", "hello"))
                                .withUrl(url)
                                .withDescription(new I18nProperty("Description", "description"))
                                .build())
                        .build()
        );

        try
        {
            String descriptor = connectAddonRegistry.getDescriptor(key);
            assertFalse(Strings.isNullOrEmpty(descriptor));

            JsonNode descriptorNode = new ObjectMapper().readTree(descriptor);
            JsonNode urlNode = descriptorNode.path("modules").path("jiraSearchRequestViews").path(0).path("url");
            assertFalse(urlNode.isMissingNode());
            assertEquals(url, urlNode.asText());
        }
        finally
        {
            testPluginInstaller.uninstallAddon(addon);
        }
    }

    @BeforeClass
    public void beforeAnyTests()
    {
        testAuthenticator.authenticateUser("admin");
    }

    @AfterClass
    public void afterAllTests()
    {
        testAuthenticator.unauthenticate();
    }
}
