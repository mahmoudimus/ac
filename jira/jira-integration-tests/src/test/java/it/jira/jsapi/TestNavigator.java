package it.jira.jsapi;

import com.atlassian.connect.test.jira.pageobjects.RemoteNavigatorGeneralPage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import it.jira.JiraWebDriverTestBase;
import it.jira.servlet.JiraAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static junit.framework.TestCase.assertEquals;

public class TestNavigator extends JiraWebDriverTestBase {
    private static final String PAGE_KEY = "ac-navigator-general-page";
    private static ConnectRunner remotePlugin;

    @BeforeClass
    public static void startConnectAddOn() throws Exception {
        remotePlugin = new ConnectRunner(JiraWebDriverTestBase.product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddonKey())
                .setAuthenticationToNone()
                .addModules("generalPages",
                        newPageBean()
                                .withName(new I18nProperty("Nvg", null))
                                .withUrl("/nvg")
                                .withKey(PAGE_KEY)
                                .withLocation("system.header/left")
                                .build()
                )
                .addRoute("/nvg", JiraAppServlets.navigatorServlet())
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception {
        if (remotePlugin != null) {
            remotePlugin.stopAndUninstall();
        }
    }

    public RemoteNavigatorGeneralPage visitNavigatorPage() {
        return loginAndVisit(testUserFactory.basicUser(),
                RemoteNavigatorGeneralPage.class, remotePlugin.getAddon().getKey(), PAGE_KEY);
    }

    /**
     * This test makes sure the Navigation API is not available in JIRA and raises appropriate errors
     * if its called.  When/If the navigation API is implemented for JIRA this test can change to validate
     * it (see: it.confluence.jsapi.TestNavigator).
     *
     * @throws Exception
     */
    @Test
    public void testNavigatorNotAvailable() throws Exception {
        RemoteNavigatorGeneralPage page = visitNavigatorPage();
        assertEquals("navigator should be a function", "function", page.getMessage("navigator-type"));
        assertEquals("navigator.go should be a function", "function", page.getMessage("navigator-go"));
    }

}
