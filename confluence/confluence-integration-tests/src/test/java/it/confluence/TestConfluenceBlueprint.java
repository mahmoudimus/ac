package it.confluence;

import com.atlassian.confluence.pageobjects.page.DashboardPage;

import org.apache.log4j.Level;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static it.confluence.servlet.ConfluenceAppServlets.blueprintContextServlet;
import static it.confluence.servlet.ConfluenceAppServlets.blueprintTemplateServlet;
import static it.confluence.servlet.ConfluenceAppServlets.blueprintMalformedContextServlet;
import static it.confluence.servlet.ConfluenceAppServlets.blueprint404Servlet;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Integration test that loads a blueprint addon and uses it in confluence.
 */
public final class TestConfluenceBlueprint extends ConfluenceWebDriverTestBase
{
    @BeforeClass
    public static void setupConfluenceAndStartConnectAddon()
    {
        rpc.setLogLevel("com.atlassian.plugin.connect.confluence", Level.DEBUG);
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception
    {
        rpc.setLogLevel("com.atlassian.plugin.connect.confluence", Level.INFO);
    }

    @Test
    public void testRemoteSimpleBlueprintVisibleInDialog() throws Exception
    {
        ConfluenceBlueprintTestHelper.runInRunner(product, blueprintTemplateServlet(), blueprintContextServlet(), (helper) -> {
            login(testUserFactory.basicUser());
            product.visit(DashboardPage.class)
                    .openCreateDialog()
                    .waitForBlueprint(helper.getCompleteKey());
        });
    }

    @Test
    public void testRemoteSimpleBlueprintCanCreatePage() throws Exception
    {
        ConfluenceBlueprintTestHelper.runInRunner(product, blueprintTemplateServlet(), blueprintContextServlet(), (helper) -> {
            login(testUserFactory.basicUser());
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
        });
    }

    @Test
    public void testShowErrorTextWhenCantRetrieveContext() throws Exception
    {
        ConfluenceBlueprintTestHelper.runInRunner(product, blueprintTemplateServlet(), blueprint404Servlet(), (helper) -> {
            login(testUserFactory.basicUser());
            product.visit(DashboardPage.class).openCreateDialog();
            String errorText = product.getPageBinder()
                    .bind(CreateContentDialog.class)
                    .clickBlueprintItem(helper.getCompleteKey())
                    .getErrorLabel()
                    .getText();

            assertThat("contains error message", errorText, notNullValue());
            assertThat("contains detailed message", errorText.toLowerCase(), containsString("error while retrieving"));
        });
    }

    @Test
    public void testShowErrorTextWhenCantParseContextJSON() throws Exception
    {
        ConfluenceBlueprintTestHelper.runInRunner(product, blueprintTemplateServlet(), blueprintMalformedContextServlet(), (helper) -> {
            login(testUserFactory.basicUser());
            product.visit(DashboardPage.class).openCreateDialog();
            String errorText = product.getPageBinder()
                    .bind(CreateContentDialog.class)
                    .clickBlueprintItem(helper.getCompleteKey())
                    .getErrorLabel()
                    .getText();

            assertThat("contains error message", errorText, notNullValue());
            assertThat("contains detailed message", errorText.toLowerCase(), containsString("json syntax error"));
        });
    }
}
