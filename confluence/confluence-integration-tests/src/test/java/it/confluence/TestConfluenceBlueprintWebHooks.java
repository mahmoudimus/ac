package it.confluence;

import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.plugin.connect.test.common.servlet.WebHookTestServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.webhook.WebHookBody;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public final class TestConfluenceBlueprintWebHooks extends ConfluenceWebDriverTestBase
{
    private static String RETROSPECTIVES_ITEM_KEY = "com.atlassian.confluence.plugins"
            + ".confluence-software-blueprints:retrospectives-item";

    @Test
    public void testBlueprintPageCreatedWebHookFired() throws Exception
    {
        // This test case currently need web driver as Blueprint APIs are not ready
        WebHookTestServlet.runInJsonRunner(product.getProductInstance().getBaseUrl(), "blueprint_page_created", waiter -> {
            try
            {
                String title = "Test page for blueprint webhook test" + AddonTestUtils.randomAddonKey();

                login(testUserFactory.basicUser());
                product.visit(DashboardPage.class).createDialog.click();
                product.getPageBinder()
                        .bind(CreateContentDialog.class)
                        .createWithBlueprintWizard(RETROSPECTIVES_ITEM_KEY)
                        .clickCreateButton()
                        .getEditContentPage()
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
    }
}
