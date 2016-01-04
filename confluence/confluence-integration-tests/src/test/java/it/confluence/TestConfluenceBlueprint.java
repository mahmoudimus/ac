package it.confluence;

import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;

import org.apache.log4j.Level;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Integration test that loads a blueprint addon and uses it in confluence.
 */
public final class TestConfluenceBlueprint extends ConfluenceWebDriverTestBase
{
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
        rpc.setLogLevel("com.atlassian.plugin.connect.confluence", Level.INFO);
    }

    @Test
    public void testRemoteSimpleBlueprintVisibleInDialog()
    {
        login(testUserFactory.basicUser());
        product.visit(DashboardPage.class).createDialog.click();
        product.getPageBinder().bind(CreateContentDialog.class).waitForBlueprint(helper.getCompleteKey());
    }

    @Test
    public void testRemoteSimpleBlueprintCanCreatePage()
    {
        product.visit(DashboardPage.class).openCreateDialog();
        String editorHtml = product.getPageBinder()
                                   .bind(CreateContentDialog.class)
                                   .createWithBlueprint(helper.getCompleteKey())
                                   .getEditor()
                                   .getContent()
                                   .getTimedHtml()
                                   .byDefaultTimeout();
        Document editorDom = Jsoup.parseBodyFragment(editorHtml);
        assertThat("new page includes blueprint content", editorHtml, containsString("Hello Blueprint"));
        assertThat("new page includes blueprint content", editorHtml, containsString(helper.getModuleKey()));
        assertThat("wiki variable failed", editorDom.select("p strong").text(), is(helper.getKey()));
        assertThat("xhtml variable failed", editorDom.select("h2 img").attr("data-macro-name"), is("cheese"));
    }
}
