package it.com.atlassian.plugin.connect.jira;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.plugin.connect.util.auth.TestAuthenticator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.SearchRequestViewModuleBean.newSearchRequestViewModuleBean;
import static org.junit.Assert.assertEquals;

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
            assertEquals(url, new com.google.gson.JsonParser().parse(connectAddonRegistry.getDescriptor(key)).getAsJsonObject()
                    .get("modules").getAsJsonObject()
                    .get("jiraSearchRequestViews").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("url").getAsString());
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
