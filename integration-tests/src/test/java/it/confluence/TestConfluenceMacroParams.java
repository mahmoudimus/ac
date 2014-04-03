package it.confluence;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceMacroPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceMacroTestSuitePage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.*;
import it.servlet.ConnectAppServlets;
import org.junit.After;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.test.Utils.loadResourceAsString;
import static com.google.common.collect.Maps.newHashMap;
import static it.TestConstants.BETTY_USERNAME;
import static org.junit.Assert.*;

public final class TestConfluenceMacroParams extends ConfluenceWebDriverTestBase
{
    private AtlassianConnectAddOnRunner remotePlugin;

    @After
    public void cleanup() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
            remotePlugin = null;
        }
    }

    @Test
    public void testContextParam() throws Exception
    {
        ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(new ConfluenceOps.ConfluenceUser("admin", "admin")), "ds", "test", loadResourceAsString("confluence/test-page.xhtml"));

        remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
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
                        .contextParameters(ContextParameter.name("page.id").query())
                        .editor(MacroEditor.at("/myMacroEditor").height("600").width("600").resource(ConnectAppServlets.macroEditor()))
                        .resource(new TestConfluencePageMacro.MyMacroServlet()))
                .add(GeneralPageModule.key("remotePluginGeneral")
                        .name("Remotable Plugin app1 General")
                        .path("/page?page_id=${page.id}") // TODO: this is not working wo the $. As it is deprecated I didn't chance down why
                        .linkName("Remotable Plugin app1 General Link")
                        .iconUrl("/public/sandcastles.jpg")
                        .height("600")
                        .width("700")
                        .resource(ConnectAppServlets.helloWorldServlet()))
                .addRoute("/page/*", ConnectAppServlets.helloWorldServlet())
                .start();

        loginAsBetty();
        Map<String, String> params = product.visit(ConfluenceMacroTestSuitePage.class, pageData.getTitle())
                                            .visitGeneralLink()
                                            .getIframeQueryParams();

        assertEquals(pageData.getId(), params.get("page_id"));
        assertEquals(BETTY_USERNAME, params.get("user_id"));
        assertTrue(params.containsKey("user_key"));
    }

    @Test
    public void testMacroWithHeaderParams() throws Exception
    {
        ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(new ConfluenceOps.ConfluenceUser("admin", "admin")), "ds", "test",
                "<div class=\"header-macro\">\n" +
                        "   <ac:macro ac:name=\"header\" />\n" +
                        "</div>");

        MyParamsMacroServlet macroServlet = new MyParamsMacroServlet();
        remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl(), "header")
                .add(RemoteMacroModule.key("header")
                                      .path("/header")
                                      .contextParameters(
                                              ContextParameter.name("page_id").query(),
                                              ContextParameter.name("user_id").header())
                                      .resource(macroServlet))
                .start();
        loginAsBetty();
        product.visit(ConfluenceMacroPage.class, pageData.getTitle());
        assertEquals(pageData.getId(), macroServlet.getQueryParams().get("page_id"));
        assertFalse(macroServlet.getQueryParams().containsKey("user_id"));
        assertEquals(BETTY_USERNAME, macroServlet.getHeaderParams().get("user_id"));
        assertFalse(macroServlet.getHeaderParams().containsKey("page_id"));
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
