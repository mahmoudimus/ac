package it.jira.item;

import java.util.Optional;

import com.atlassian.jira.pageobjects.pages.admin.configuration.ViewGeneralConfigurationPage;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import it.jira.JiraWebDriverTestBase;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;

public class TestJiraWebItemWithProductCondition extends JiraWebDriverTestBase
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
    public void shouldPerformActionForWebItemWithAdminModeCondition()
    {
        loginAndVisit(testUserFactory.admin(), ViewGeneralConfigurationPage.class);
        RemoteWebItem webItem = connectPageOperations.findWebItem(addonAndModuleKey(addon.getAddon().getKey(), ITEM_KEY), Optional.<String>empty());
        webItem.click();
        connectPageOperations.getPageBinder().bind(ConnectAddOnEmbeddedTestPage.class, addon.getAddon().getKey(), ITEM_KEY, true);
    }
}
