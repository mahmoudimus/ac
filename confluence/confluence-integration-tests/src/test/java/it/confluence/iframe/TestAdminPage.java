package it.confluence.iframe;

import com.atlassian.confluence.pageobjects.page.admin.ConfluenceAdminHomePage;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceAdminPage;
import com.atlassian.connect.test.confluence.pageobjects.ConnectConfluenceAdminHomePage;
import com.atlassian.pageobjects.elements.timeout.DefaultTimeouts;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.pageobjects.InsufficientPermissionsPage;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.util.IframeUtils;
import it.confluence.ConfluenceWebDriverTestBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.net.URI;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.atlassian.pageobjects.elements.query.Queries.forSupplier;
import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.test.common.servlet.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test of general page in Confluence
 */
public class TestAdminPage extends ConfluenceWebDriverTestBase {
    private static final String PLUGIN_KEY = AddonTestUtils.randomAddonKey();

    private static final String PAGE_NAME = "My Admin Page";
    private static final String PAGE_KEY = "my-admin-page";

    private static ConnectRunner remotePlugin;

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    @BeforeClass
    public static void startConnectAddon() throws Exception {
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
    public static void stopConnectAddon() throws Exception {
        if (remotePlugin != null) {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void canClickOnPageLinkAndSeeAddonContents() throws Exception {
        loginAndVisit(testUserFactory.admin(), ConfluenceAdminHomePage.class);

        ConfluenceAdminPage adminPage = product.getPageBinder().bind(ConfluenceAdminPage.class, PLUGIN_KEY, PAGE_KEY);

        URI url = new URI(adminPage.getRemotePluginLinkHref());
        assertThat(url.getPath(), is("/confluence" + IframeUtils.iframeServletPath(PLUGIN_KEY, PAGE_KEY)));

        // TODO Admin page web-item location has incorrect text ("OSGi")

        final ConnectAddonEmbeddedTestPage addonContentsPage = adminPage.clickAddonLink();
        waitUntilTrue(forSupplier(new DefaultTimeouts(),
                () -> "Hello world".equals(addonContentsPage.getValueById("hello-world-message"))));
    }

    @Test
    public void nonAdminCanNotSeePage() {
        login(testUserFactory.basicUser());
        InsufficientPermissionsPage page = product.visit(InsufficientPermissionsPage.class, PLUGIN_KEY, PAGE_KEY);
        assertThat(page.getErrorMessage(), containsString("You do not have the correct permissions"));
        assertThat(page.getErrorMessage(), containsString("My Admin Page"));
    }

    @Test
    public void pageIsNotAccessibleWithFalseCondition() {
        remotePlugin.setToggleableConditionShouldDisplay(false);

        // web item should not be displayed
        ConnectConfluenceAdminHomePage adminPage = loginAndVisit(testUserFactory.admin(), ConnectConfluenceAdminHomePage.class);
        assertThat("Expected web-item for page to NOT be present", adminPage.getWebItem(PAGE_KEY).isPresent(), is(false));

        // directly retrieving page should result in access denied
        InsufficientPermissionsPage insufficientPermissionsPage = product.visit(InsufficientPermissionsPage.class, PLUGIN_KEY, PAGE_KEY);
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString("You do not have the correct permissions"));
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString("My Admin Page"));
    }


}
