package it.jira.iframe;

import com.atlassian.connect.test.jira.pageobjects.JiraAdminPage;
import com.atlassian.jira.pageobjects.pages.JiraAdminHomePage;
import com.atlassian.jira.pageobjects.pages.admin.configuration.ViewGeneralConfigurationPage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.pageobjects.InsufficientPermissionsPage;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.util.IframeUtils;
import it.jira.JiraWebDriverTestBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.test.common.servlet.ToggleableConditionServlet.toggleableConditionBean;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test of general page in JIRA
 */
public class TestAdminPage extends JiraWebDriverTestBase {
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
                                .withConditions(toggleableConditionBean())
                                .withUrl("/pg")
                                .withWeight(1234)
                                .build())
                .addRoute("/pg", ConnectAppServlets.sizeToParentServlet())
                .start();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception {
        if (remotePlugin != null) {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void canClickOnPageLinkAndSeeAddonContents() throws MalformedURLException, URISyntaxException {
        loginAndVisit(testUserFactory.admin(), ViewGeneralConfigurationPage.class);

        JiraAdminPage adminPage = product.getPageBinder().bind(JiraAdminPage.class, PLUGIN_KEY, PAGE_KEY);

        URI url = new URI(adminPage.getRemotePluginLinkHref());
        assertThat(url.getPath(), is("/jira" + IframeUtils.iframeServletPath(PLUGIN_KEY, PAGE_KEY)));

        ConnectAddonEmbeddedTestPage addonContentsPage = adminPage.clickAddonLink();
        assertEquals("Hello world", addonContentsPage.getValueBySelector("#hello-world-message"));
    }

    @Test
    public void addonPageIsFullSize() throws MalformedURLException, URISyntaxException {
        loginAndVisit(testUserFactory.admin(), ViewGeneralConfigurationPage.class);

        JiraAdminPage adminPage = product.getPageBinder().bind(JiraAdminPage.class, PLUGIN_KEY, PAGE_KEY);

        ConnectAddonEmbeddedTestPage addonContentsPage = adminPage.clickAddonLink();
        assertTrue("Addon is full size", addonContentsPage.isFullSize());
    }

    @Test
    public void nonAdminCanNotSeePage() {
        InsufficientPermissionsPage page = loginAndVisit(testUserFactory.basicUser(), InsufficientPermissionsPage.class, PLUGIN_KEY, PAGE_KEY);
        assertThat(page.getErrorMessage(), containsString("You do not have the correct permissions"));
        assertThat(page.getErrorMessage(), containsString("My Admin Page"));
    }

    @Test
    public void pageIsNotAccessibleWithFalseCondition() {
        remotePlugin.setToggleableConditionShouldDisplay(false);

        login(testUserFactory.admin());

        // web item should not be displayed
        product.visit(JiraAdminHomePage.class);
        assertThat("Expected web-item for page to NOT be present", connectPageOperations.existsWebItem(PAGE_KEY), is(false));

        // directly retrieving page should result in access denied
        InsufficientPermissionsPage insufficientPermissionsPage = product.visit(InsufficientPermissionsPage.class, PLUGIN_KEY, PAGE_KEY);
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString("You do not have the correct permissions"));
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString(PAGE_NAME));
    }


}
