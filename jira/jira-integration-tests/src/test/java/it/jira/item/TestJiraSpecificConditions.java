package it.jira.item;

import com.atlassian.jira.pageobjects.pages.admin.configuration.ViewGeneralConfigurationPage;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.jira.JiraWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;

public class TestJiraSpecificConditions extends JiraWebDriverTestBase
{

    private static final String ITEM_KEY = "admin-mode-only-item";
    private static final String ITEM_URL = "/adminModeOnly";

    private static ConnectRunner addon;

    @BeforeClass
    public static void startAddon() throws Exception
    {
        addon = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone()
                .addModules("webItems", newWebItemBean()
                        .withKey(ITEM_KEY)
                        .withUrl(ITEM_URL)
                        .withLocation("system.top.navigation.bar")
                        .withTarget(newWebItemTargetBean().withType(WebItemTargetType.dialog).build())
                        .withName(new I18nProperty("Admin mode only", null))
                        .withConditions(
                                newSingleConditionBean().withCondition("is_admin_mode").build()
                        )
                        .build())
                .addRoute(ITEM_URL, ConnectAppServlets.helloWorldServlet())
                .start();
    }

    @AfterClass
    public static void stopAddon() throws Exception
    {
        if (addon != null)
        {
            addon.stopAndUninstall();
        }
    }

    @Test
    public void shouldDisplayItemWithAdminModeCondition()
    {
        loginAndVisit(testUserFactory.admin(), ViewGeneralConfigurationPage.class);
        RemoteWebItem webItem = connectPageOperations.findWebItem(addonAndModuleKey(addon.getAddon().getKey(), ITEM_KEY),
                com.google.common.base.Optional.<String>absent());
        webItem.click();
        connectPageOperations.getPageBinder().bind(ConnectAddOnEmbeddedTestPage.class, addon.getAddon().getKey(), ITEM_KEY, true);
    }
}
