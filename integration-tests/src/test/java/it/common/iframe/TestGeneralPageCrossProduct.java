package it.common.iframe;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.AccessDeniedIFramePage;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginAwarePage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.common.MultiProductWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.condition.CheckUsernameConditionServlet;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestGeneralPageCrossProduct extends MultiProductWebDriverTestBase
{
    public static final String BETTY_PAGE_NAME = "Betty";
    public static final String ENCODED_SPACES_PAGE_NAME = "Enc";
    public static final String SIZE_TO_PARENT_GENERAL_PAGE = "SzP";

    private static ConnectRunner remotePlugin;

    private static TestUser betty;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        betty = testUserFactory.admin();

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .addJWT()
                .addModules("generalPages",
                        newPageBean()
                                .withKey("onlyBetty")
                                .withName(new I18nProperty(BETTY_PAGE_NAME, null))
                                .withUrl("/ob")
                                .withLocation(getGloballyVisibleLocation())
                                .withConditions(
                                        newSingleConditionBean()
                                                .withCondition("user_is_logged_in")
                                                .build(),
                                        newSingleConditionBean()
                                                .withCondition("/only" + betty.getDisplayName() + "Condition")
                                                .build())
                                .build(),
                        newPageBean()
                                .withKey("encodedSpaces")
                                .withName(new I18nProperty(ENCODED_SPACES_PAGE_NAME, null))
                                .withUrl("/my?bologne=O%20S%20C%20A%20R")
                                .withLocation(getGloballyVisibleLocation())
                                .build(),
                        newPageBean()
                                .withKey("sizeToParent")
                                .withName(new I18nProperty(SIZE_TO_PARENT_GENERAL_PAGE, null))
                                .withUrl("/fsg")
                                .withLocation(getGloballyVisibleLocation())
                                .build())
                .addRoute("/rpg", ConnectAppServlets.apRequestServlet())
                .addRoute("/ob", ConnectAppServlets.helloWorldServlet())
                .addRoute("/only" + betty.getDisplayName() + "Condition", new CheckUsernameConditionServlet(betty))
                .addRoute("/my", ConnectAppServlets.helloWorldServlet())
                .addRoute("/fsg", ConnectAppServlets.sizeToParentServlet())
                .addScope(ScopeName.READ)
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
    public void testNoAdminPageForNonAdmin()
    {
        login(testUserFactory.basicUser());
        AccessDeniedIFramePage page = product.getPageBinder().bind(AccessDeniedIFramePage.class, "app1", "remotePluginAdmin");
        assertFalse(page.isIframeAvailable());
    }

    @Test
    public void testRemoteConditionSucceeds()
    {
        loginAndVisit(betty, HomePage.class);
        GeneralPage page = product.getPageBinder().bind(GeneralPage.class, "onlyBetty", BETTY_PAGE_NAME, remotePlugin.getAddon().getKey());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();

        assertTrue(remotePluginTest.getTitle().contains(BETTY_PAGE_NAME));
    }

    @Test
    public void testEncodedSpaceInPageModuleUrl()
    {
        // Regression test for AC-885 (ensure descriptor query strings are not decoded before parsing)
        loginAndVisit(testUserFactory.admin(), HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "encodedSpaces", ENCODED_SPACES_PAGE_NAME, remotePlugin.getAddon().getKey());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();

        assertThat(remotePluginTest.getValueBySelector("#hello-world-message"), is("Hello world"));
    }

    @Test
    public void testSizeToParent()
    {
        loginAndVisit(testUserFactory.admin(), HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "sizeToParent", SIZE_TO_PARENT_GENERAL_PAGE, remotePlugin.getAddon().getKey());
        ConnectAddOnEmbeddedTestPage remotePluginTest = page.clickAddOnLink();

        assertTrue(remotePluginTest.isFullSize());
    }
}
