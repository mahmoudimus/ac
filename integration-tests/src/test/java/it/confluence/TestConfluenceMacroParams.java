package it.confluence;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.remotable.junit.HtmlDumpRule;
import com.atlassian.plugin.remotable.spi.Permissions;
import com.atlassian.plugin.remotable.test.pageobjects.OwnerOfTestedProduct;
import com.atlassian.plugin.remotable.test.pageobjects.confluence.ConfluenceMacroPage;
import com.atlassian.plugin.remotable.test.pageobjects.confluence.ConfluenceMacroTestSuitePage;
import com.atlassian.plugin.remotable.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.remotable.test.pageobjects.confluence.FixedConfluenceTestedProduct;
import com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.remotable.test.server.module.ContextParameter;
import com.atlassian.plugin.remotable.test.server.module.GeneralPageModule;
import com.atlassian.plugin.remotable.test.server.module.MacroCategory;
import com.atlassian.plugin.remotable.test.server.module.MacroEditor;
import com.atlassian.plugin.remotable.test.server.module.MacroParameter;
import com.atlassian.plugin.remotable.test.server.module.RemoteMacroModule;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;

import static com.atlassian.plugin.remotable.test.Utils.createSignedRequestHandler;
import static com.atlassian.plugin.remotable.test.Utils.loadResourceAsString;
import static com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner.newMustacheServlet;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestConfluenceMacroParams
{

    private static final TestedProduct<WebDriverTester> product;
    private static final ConfluenceOps confluenceOps;

    static
    {
        System.setProperty("testedProductClass", FixedConfluenceTestedProduct.class.getName());
        product = OwnerOfTestedProduct.INSTANCE;
        confluenceOps = new ConfluenceOps(product.getProductInstance().getBaseUrl());
    }

    @Rule
    public HtmlDumpRule htmlDump = new HtmlDumpRule(product.getTester().getDriver());

    @After
    public void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    @Test
    public void testContextParam() throws Exception
    {
        AtlassianConnectAddOnRunner remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl(), "app1")
                .addOAuth(createSignedRequestHandler("app1"))
                .addPermission(Permissions.CREATE_OAUTH_LINK)
                .addPermission("read_content")
                .addPermission("read_users_and_groups")
                .addPermission("read_server_information")
                .add(RemoteMacroModule.key("app1-macro")
                        .name("app1-macro")
                        .title("Remotable Plugin app1 Macro")
                        .path("/app1-macro")
                        .iconUrl("/public/sandcastles.jpg")
                        .outputType("block")
                        .bodyType("rich-text")
                        .featured("true")
                        .category(MacroCategory.name("development"))
                        .parameters(MacroParameter.name("footy").title("Favorite Footy").type("enum").required("true").values("American Football", "Soccer", "Rugby Union", "Rugby League"))
                        .contextParameters(ContextParameter.name("page_id").type("query"))
                        .editor(MacroEditor.at("/myMacroEditor").height("600").width("600").resource(new TestConfluence.MyMacroEditorServlet()))
                        .resource(new TestConfluence.MyMacroServlet()))
                .add(GeneralPageModule.key("remotePluginGeneral")
                        .name("Remotable Plugin app1 General")
                        .path("/rpg")
                        .linkName("Remotable Plugin app1 General Link")
                        .iconUrl("/public/sandcastles.jpg")
                        .height("600")
                        .width("700")
                        .resource(newMustacheServlet("iframe.mu")))
                .start();

        Map pageData = confluenceOps.setPage("ds", "test", loadResourceAsString("confluence/test-page.xhtml"));
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        Map<String, String> params = product.visit(ConfluenceMacroTestSuitePage.class, pageData.get("title"))
                .visitGeneralLink()
                .getIframeQueryParams();

        assertEquals(pageData.get("id"), params.get("page_id"));

        remotePlugin.stop();
    }

    @Test
    public void testMacroWithHeaderParams() throws Exception
    {
        Map pageData = confluenceOps.setPage("ds", "test",
                "<div class=\"header-macro\">\n" +
                        "   <ac:macro ac:name=\"header\" />\n" +
                        "</div>");

        MyParamsMacroServlet macroServlet = new MyParamsMacroServlet();
        AtlassianConnectAddOnRunner runner = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl(), "header")
                .addMacro("header", "/header", macroServlet, asList(
                        asList("page_id", "query"),
                        asList("user_id", "header")
                ))
                .start();
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        product.visit(ConfluenceMacroPage.class, pageData.get("title"));
        assertEquals(pageData.get("id"), macroServlet.getQueryParams().get("page_id"));
        assertFalse(macroServlet.getQueryParams().containsKey("user_id"));
        assertEquals("admin", macroServlet.getHeaderParams().get("user_id"));
        assertFalse(macroServlet.getHeaderParams().containsKey("page_id"));
        runner.stop();
    }

    public static class MyParamsMacroServlet extends HttpServlet
    {
        private Map<String, String> headerParams = newHashMap();
        private Map<String, String> queryParams = newHashMap();

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            for (Enumeration<String> names = req.getHeaderNames(); names.hasMoreElements(); )
            {
                String rawName = names.nextElement();
                if (rawName.startsWith("AP-CTX-"))
                {
                    String name = rawName.substring("AP-CTX-".length()).replace('-', '_').toLowerCase();
                    String value = req.getHeader(rawName);
                    headerParams.put(name, value);
                }
            }

            for (Enumeration<String> names = req.getParameterNames(); names.hasMoreElements(); )
            {
                String rawName = names.nextElement();
                if (rawName.startsWith("ctx_"))
                {
                    String name = rawName.substring("ctx_".length());
                    String value = req.getParameter(rawName);
                    queryParams.put(name, value);
                }
            }
            PrintWriter out = resp.getWriter();
            resp.setContentType("text/html");
            out.write("<p>hi</p>");
            out.close();
        }

        public Map<String, String> getHeaderParams()
        {
            return headerParams;
        }

        public Map<String, String> getQueryParams()
        {
            return queryParams;
        }
    }
}
