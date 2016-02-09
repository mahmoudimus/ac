package it.confluence;

import com.atlassian.confluence.pageobjects.page.DashboardPage;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import org.apache.log4j.Level;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static it.confluence.servlet.ConfluenceAppServlets.blueprint404Servlet;
import static it.confluence.servlet.ConfluenceAppServlets.blueprintContextServlet;
import static it.confluence.servlet.ConfluenceAppServlets.blueprintMalformedContextServlet;
import static it.confluence.servlet.ConfluenceAppServlets.blueprintTemplateServlet;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Integration test that loads a blueprint addon and uses it in confluence.
 */
public final class TestConfluenceBlueprint extends ConfluenceWebDriverTestBase
{
    //use only 1 static user since the default test license contains a limit of 5
    private static TestUser user;

    @BeforeClass
    public static void setupConfluenceAndStartConnectAddon()
    {
        rpc.setLogLevel("com.atlassian.plugin.connect.confluence", Level.DEBUG);
        user = testUserFactory.basicUser();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception
    {
        rpc.setLogLevel("com.atlassian.plugin.connect.confluence", Level.INFO);
    }

    @Test
    public void testRemoteSimpleBlueprintVisibleInDialog() throws Exception
    {
        ConfluenceBlueprintTestHelper.run(product, blueprintTemplateServlet("it/confluence/blueprint/blueprint.mu"), (helper) -> {
            login(user);
            product.visit(DashboardPage.class)
                    .openCreateDialog()
                    .waitForBlueprint(helper.getCompleteKey());
        });

        ConfluenceBlueprintTestHelper.runWithBlueprintContext(product, blueprintTemplateServlet("it/confluence/blueprint/blueprint.mu"), blueprintContextServlet(), (helper) -> {
            login(user);
            product.visit(DashboardPage.class)
                    .openCreateDialog()
                    .waitForBlueprint(helper.getCompleteKey());
        });
    }

    @Test
    public void testRemoteSimpleBlueprintCanCreatePage() throws Exception
    {
        ConfluenceBlueprintTestHelper.run(product, blueprintTemplateServlet("it/confluence/blueprint/blueprint-no-context.mu"), (helper) -> {
            login(user);
            product.visit(DashboardPage.class).openCreateDialog();
            String editorHtml = product.getPageBinder()
                    .bind(CreateContentDialog.class)
                    .createWithBlueprint(helper.getCompleteKey())
                    .getEditor()
                    .getContent()
                    .getTimedHtml()
                    .byDefaultTimeout();
            assertThat("new page includes blueprint content", editorHtml, containsString("Hello Blueprint"));
        });
    }

    @Test
    public void testRemoteSimpleBlueprintWithContextCanCreatePage() throws Exception
    {
        ConfluenceBlueprintTestHelper.runWithBlueprintContext(product, blueprintTemplateServlet(
                "it/confluence/blueprint/blueprint.mu"), blueprintContextServlet(), (helper) -> {
            login(user);
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
        ConfluenceBlueprintTestHelper.runWithBlueprintContext(product, blueprintTemplateServlet(
                "it/confluence/blueprint/blueprint.mu"), blueprint404Servlet(), (helper) -> {
            login(user);
            product.visit(DashboardPage.class).openCreateDialog();
            String errorText = product.getPageBinder()
                    .bind(CreateContentDialog.class)
                    .clickBlueprintItem(helper.getCompleteKey())
                    .getErrorMessage()
                    .getText();

            assertThat("contains error message", errorText, notNullValue());
            assertThat("contains detailed message", errorText.toLowerCase(), containsString("a problem with"));
        });
    }

    @Test
    public void testShowErrorTextWhenCantParseContextJSON() throws Exception
    {
        ConfluenceBlueprintTestHelper.runWithBlueprintContext(product, blueprintTemplateServlet(
                "it/confluence/blueprint/blueprint.mu"), blueprintMalformedContextServlet(), (helper) -> {
            login(user);
            product.visit(DashboardPage.class).openCreateDialog();
            String errorText = product.getPageBinder()
                    .bind(CreateContentDialog.class)
                    .clickBlueprintItem(helper.getCompleteKey())
                    .getErrorMessage()
                    .getText();

            assertThat("contains error message", errorText, notNullValue());
            assertThat("contains detailed message", errorText.toLowerCase(), containsString("problem parsing response"));
        });
    }
}
