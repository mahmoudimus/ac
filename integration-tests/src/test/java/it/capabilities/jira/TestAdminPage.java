package it.capabilities.jira;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.InsufficientPermissionsPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginTestPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraAdminPage;
import com.atlassian.plugin.connect.test.pageobjects.jira.JiraAdministrationHomePage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.TestConstants;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static it.jira.TestJira.EXTRA_PREFIX;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test of general page in JIRA
 */
public class TestAdminPage extends JiraWebDriverTestBase
{
    private static final String PLUGIN_KEY = "my-plugin";

    private static final String PAGE_NAME = "My Admin Page";
    private static final String GENERATED_PAGE_KEY = "my-admin-page";

    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .addModule(
                        "adminPages",
                        newPageBean()
                                .withName(new I18nProperty(PAGE_NAME, null))
                                .withKey(GENERATED_PAGE_KEY)
                                .withUrl("/pg")
                                .withWeight(1234)
                                .build())
                .addRoute("/pg", ConnectAppServlets.helloWorldServlet())
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void canClickOnPageLinkAndSeeAddonContents() throws MalformedURLException, URISyntaxException
    {
        loginAsAdmin();
        product.visit(JiraAdministrationHomePage.class, EXTRA_PREFIX);

        JiraAdminPage adminPage = product.getPageBinder().bind(JiraAdminPage.class, GENERATED_PAGE_KEY, PAGE_NAME);

        assertThat(adminPage.isRemotePluginLinkPresent(), is(true));

        URI url = new URI(adminPage.getRemotePluginLinkHref());
        assertThat(url.getPath(), is("/jira/plugins/servlet/ac/my-plugin/" + GENERATED_PAGE_KEY));

        RemotePluginTestPage addonContentsPage = adminPage.clickRemotePluginLink();
        assertEquals("Hello world", addonContentsPage.getValueBySelector("#hello-world-message"));
    }

    @Test
    public void nonAdminCanNotSeePage()
    {
        loginAs(TestConstants.BARNEY_USERNAME, TestConstants.BARNEY_USERNAME);
        InsufficientPermissionsPage page = product.visit(InsufficientPermissionsPage.class, "my-plugin", GENERATED_PAGE_KEY);
        assertThat(page.getErrorMessage(), containsString("You do not have the correct permissions"));
        assertThat(page.getErrorMessage(), containsString("My Admin Page"));
    }

}
