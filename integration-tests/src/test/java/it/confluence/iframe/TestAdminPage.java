package it.confluence.iframe;

import java.net.URI;

import com.atlassian.confluence.pageobjects.page.admin.ConfluenceAdminHomePage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.InsufficientPermissionsPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceAdminPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConnectConfluenceAdminHomePage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;

import it.util.ConnectTestUserFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import it.confluence.ConfluenceWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.util.TestUser;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static it.servlet.condition.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test of general page in Confluence
 */
public class TestAdminPage extends ConfluenceWebDriverTestBase
{
    private static final String PLUGIN_KEY = AddonTestUtils.randomAddOnKey();

    private static final String PAGE_NAME = "My Admin Page";
    private static final String PAGE_KEY = "my-admin-page";

    private static ConnectRunner remotePlugin;

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                .setAuthenticationToNone()
                .addModule(
                        "adminPages",
                        newPageBean()
                                .withName(new I18nProperty(PAGE_NAME, null))
                                .withKey(PAGE_KEY)
                                .withUrl("/pg")
                                .withWeight(1234)
                                .withConditions(toggleableConditionBean())
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
    public void canClickOnPageLinkAndSeeAddonContents() throws Exception
    {
        loginAndVisit(ConnectTestUserFactory.admin(product), ConfluenceAdminHomePage.class);

        ConfluenceAdminPage adminPage = product.getPageBinder().bind(ConfluenceAdminPage.class, PLUGIN_KEY, PAGE_KEY);

        assertThat(adminPage.isRemotePluginLinkPresent(), is(true));

        URI url = new URI(adminPage.getRemotePluginLinkHref());
        assertThat(url.getPath(), is("/confluence/plugins/servlet/ac/" + PLUGIN_KEY + "/" + PAGE_KEY));

        // TODO Admin page web-item location has incorrect text ("OSGi")

        ConnectAddOnEmbeddedTestPage addonContentsPage = adminPage.clickAddOnLink();
        assertEquals("Hello world", addonContentsPage.getValueBySelector("#hello-world-message"));
    }

    @Test
    public void nonAdminCanNotSeePage()
    {
        login(ConnectTestUserFactory.basicUser(product));
        InsufficientPermissionsPage page = product.visit(InsufficientPermissionsPage.class, PLUGIN_KEY, PAGE_KEY);
        assertThat(page.getErrorMessage(), containsString("You do not have the correct permissions"));
        assertThat(page.getErrorMessage(), containsString("My Admin Page"));
    }

    @Test
    public void pageIsNotAccessibleWithFalseCondition()
    {
        remotePlugin.setToggleableConditionShouldDisplay(false);

        // web item should not be displayed
        ConnectConfluenceAdminHomePage adminPage = loginAndVisit(ConnectTestUserFactory.admin(product), ConnectConfluenceAdminHomePage.class);
        assertThat("Expected web-item for page to NOT be present", adminPage.getWebItem(PAGE_KEY).isPresent(), is(false));

        // directly retrieving page should result in access denied
        InsufficientPermissionsPage insufficientPermissionsPage = product.visit(InsufficientPermissionsPage.class, PLUGIN_KEY, PAGE_KEY);
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString("You do not have the correct permissions"));
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString("My Admin Page"));
    }


}
