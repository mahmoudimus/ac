package it.confluence;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceMacroPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceMacroTestSuitePage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.*;

import org.junit.Test;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.test.Utils.loadResourceAsString;
import static com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner.newMustacheServlet;
import static com.google.common.collect.Maps.newHashMap;
import static it.TestConstants.BETTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public final class TestConfluenceMacroParams extends ConfluenceWebDriverTestBase
{
    @Test
    public void testContextParam() throws Exception
    {
        AtlassianConnectAddOnRunner remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
                .addOAuth()
                .addPermission("read_content")
                .addPermission("read_users_and_groups")
                .addPermission("read_server_information")
                .add(RemoteMacroModule.key("app1-macro")
                                      .name("app1-macro")
                                      .title("Remotable Plugin app1 Macro")
                                      .path("/app1-macro")
                                      .iconUrl("/public/sandcastles.jpg")
                                      .outputBlock()
                                      .bodyType("rich-text")
                                      .featured("true")
                                      .category(MacroCategory.name("development"))
                                      .parameters(MacroParameter.name("footy").title("Favorite Footy").type("enum").required("true").values("American Football", "Soccer", "Rugby Union", "Rugby League"))
                                      .contextParameters(ContextParameter.name("page_id").query())
                                      .editor(MacroEditor.at("/myMacroEditor").height("600").width("600").resource(newMustacheServlet("confluence/macro/editor.mu")))
                                      .resource(new TestConfluencePageMacro.MyMacroServlet()))
                .add(GeneralPageModule.key("remotePluginGeneral")
                                      .name("Remotable Plugin app1 General")
                                      .path("/rpg")
                                      .linkName("Remotable Plugin app1 General Link")
                                      .iconUrl("/public/sandcastles.jpg")
                                      .height("600")
                                      .width("700")
                                      .resource(newMustacheServlet("iframe.mu")))
                .start();

        ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(new ConfluenceOps.ConfluenceUser("admin", "admin")), "ds", "test", loadResourceAsString("confluence/test-page.xhtml"));
        product.visit(LoginPage.class).login(BETTY, BETTY, HomePage.class);
        Map<String, String> params = product.visit(ConfluenceMacroTestSuitePage.class, pageData.getTitle())
                                            .visitGeneralLink()
                                            .getIframeQueryParams();

        assertEquals(pageData.getId(), params.get("page_id"));
        assertEquals(BETTY, params.get("user_id"));
        assertFalse(params.containsKey("user_key"));

        remotePlugin.stop();
    }

    @Test
    public void testMacroWithHeaderParams() throws Exception
    {
        ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(new ConfluenceOps.ConfluenceUser("admin", "admin")), "ds", "test",
                "<div class=\"header-macro\">\n" +
                        "   <ac:macro ac:name=\"header\" />\n" +
                        "</div>");

        MyParamsMacroServlet macroServlet = new MyParamsMacroServlet();
        AtlassianConnectAddOnRunner runner = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl(), "header")
                .add(RemoteMacroModule.key("header")
                                      .path("/header")
                                      .contextParameters(
                                              ContextParameter.name("page_id").query(),
                                              ContextParameter.name("user_id").header())
                                      .resource(macroServlet))
                .start();
        product.visit(LoginPage.class).login(BETTY, BETTY, HomePage.class);
        product.visit(ConfluenceMacroPage.class, pageData.getTitle());
        assertEquals(pageData.getId(), macroServlet.getQueryParams().get("page_id"));
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
