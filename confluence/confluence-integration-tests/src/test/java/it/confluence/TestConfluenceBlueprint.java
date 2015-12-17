package it.confluence;

import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.InstallHandlerServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.util.TestUser;

import org.apache.log4j.Level;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean.newBlueprintModuleBean;
import static com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean.newBlueprintTemplateBeanBuilder;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static it.confluence.servlet.ConfluenceAppServlets.blueprintContextServlet;
import static it.confluence.servlet.ConfluenceAppServlets.blueprintTemplateServlet;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Integration test that loads a blueprint addon and uses it in confluence.
 */
public final class TestConfluenceBlueprint extends ConfluenceWebDriverTestBase
{
    private static ConnectRunner runner;
    private static String completeKey;
    private static String randomAddOnKey;
    private static String moduleKey;
    private static TestUser testUser;

    @BeforeClass
    public static void setupConfluenceAndStartConnectAddon() throws Exception
    {
        rpc.setLogLevel("com.atlassian.plugin.connect.confluence", Level.DEBUG);

        randomAddOnKey = AddonTestUtils.randomAddonKey();
        moduleKey = "my-blueprint";
        completeKey = "com.atlassian.plugins.atlassian-connect-plugin:" + addonAndModuleKey(randomAddOnKey, moduleKey) + "-web-item";
        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), randomAddOnKey)
                .addInstallLifecycle()
                .addRoute(ConnectRunner.INSTALLED_PATH, new InstallHandlerServlet())
                .addModule("blueprints",
                           newBlueprintModuleBean()
                                   .withName(new I18nProperty("My Blueprint", null))
                                   .withKey(moduleKey)
                                   .withTemplate(newBlueprintTemplateBeanBuilder()
                                                         .withUrl("/template.xml")
                                                         .withBlueprintContextUrl("/context")
                                                         .build())
                                   .build())
                .addRoute("/template.xml", blueprintTemplateServlet())
                .addRoute("/context", blueprintContextServlet())
                .addScope(ScopeName.READ)
                .start();

        testUser = testUserFactory.basicUser();
        login(testUser);

    }

    @AfterClass
    public static void stopConnectAddon() throws Exception
    {
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
        rpc.setLogLevel("com.atlassian.plugin.connect.confluence", Level.INFO);
    }

    @Test
    public void testRemoteSimpleBlueprintVisibleInDialog()
    {
        product.visit(DashboardPage.class)
               .openCreateDialog()
               .waitForBlueprint(completeKey);
    }

    @Test
    public void testRemoteSimpleBlueprintCanCreatePage()
    {
        product.visit(DashboardPage.class).createDialog.click();
        String editorHtml = product.getPageBinder()
                                   .bind(CreateContentDialog.class)
                                   .createWithBlueprint(completeKey)
                                   .getEditor()
                                   .getContent()
                                   .getTimedHtml()
                                   .byDefaultTimeout();
        Document editorDom = Jsoup.parseBodyFragment(editorHtml);
        assertThat("new page includes blueprint content", editorHtml, containsString("Hello Blueprint"));
        assertThat("new page includes blueprint content", editorHtml, containsString(moduleKey));
        assertThat("wiki variable failed", editorDom.select("p strong").text(), is(randomAddOnKey));
        assertThat("xhtml variable failed", editorDom.select("h2 img").attr("data-macro-name"), is("cheese"));
    }
}
