package it.confluence;

import java.io.IOException;

import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.InstallHandlerServlet;
import com.atlassian.plugin.connect.test.common.servlet.WebHookTestServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.webhook.WebHookBody;
import com.atlassian.plugin.connect.test.confluence.product.ConfluenceTestedProductAccessor;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import redstone.xmlrpc.XmlRpcFault;

import static com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean.newBlueprintModuleBean;
import static com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean.newBlueprintTemplateBeanBuilder;
import static it.confluence.servlet.ConfluenceAppServlets.blueprintTemplateServlet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Integration test that loads a blueprint addon and uses it in confluence.
 */
public final class TestConfluenceBlueprint extends ConfluenceWebDriverTestBase
{
    private final String baseUrl = new ConfluenceTestedProductAccessor().getConfluenceProduct().getProductInstance().getBaseUrl();
    private static ConfluenceBlueprintTestHelper helper;

    @BeforeClass
    public static void setupConfluenceAndStartConnectAddon() throws Exception
    {
        helper = ConfluenceBlueprintTestHelper.getInstance(AddonTestUtils.randomAddonKey(), product);
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception
    {
        if (helper.getRunner() != null)
        {
            helper.getRunner().stopAndUninstall();
        }
    }

    @Test
    public void testRemoteSimpleBlueprintVisibleInDialog() throws XmlRpcFault, IOException
    {
        login(testUserFactory.basicUser());
        product.visit(DashboardPage.class).createDialog.click();
        product.getPageBinder().bind(CreateContentDialog.class).waitForBlueprint(helper.getCompleteKey());
    }

    @Test
    public void testRemoteSimpleBlueprintCanCreatePage() throws XmlRpcFault, IOException
    {
        login(testUserFactory.basicUser());
        product.visit(DashboardPage.class).createDialog.click();
        assertTrue("new page includes blueprint content",
                product.getPageBinder()
                        .bind(CreateContentDialog.class)
                        .createWithBlueprint(helper.getCompleteKey())
                        .getEditor()
                        .getContent()
                        .getTimedHtml()
                        .byDefaultTimeout().contains("Hello Blueprint"));
    }

    @Test
    public void testBlueprintPageCreatedWebHookFired() throws Exception
    {
        // TODO: Move to TestConfluenceWebHooks after Blueprint APIs are ready
        // This test case currently need web driver

        WebHookTestServlet.runInJsonRunner(baseUrl, "blueprint_page_created", waiter -> {
            ConfluenceBlueprintTestHelper.runInRunner(product, (helper) -> {
                try
                {
                    String title = "Test page for " + helper.getCompleteKey();

                    login(testUserFactory.basicUser());
                    product.visit(DashboardPage.class).createDialog.click();
                    product.getPageBinder()
                            .bind(CreateContentDialog.class)
                            .createWithBlueprint(helper.getCompleteKey())
                            .setTitle(title)
                            .save();

                    final WebHookBody body = waiter.waitForHook();

                    assertNotNull(body);
                    assertEquals(body.find("page/title"), title);
                }
                catch (Exception e)
                {
                    fail("Should not throw exception: " + e.getMessage());
                }
            });
        });
    }
}
