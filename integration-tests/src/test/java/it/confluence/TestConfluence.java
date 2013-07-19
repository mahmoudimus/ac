package it.confluence;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.remotable.spi.Permissions;
import com.atlassian.plugin.remotable.test.OAuthUtils;
import com.atlassian.plugin.remotable.test.junit.HtmlDumpRule;
import com.atlassian.plugin.remotable.test.pageobjects.OwnerOfTestedProduct;
import com.atlassian.plugin.remotable.test.pageobjects.confluence.ConfluenceCounterMacroPage;
import com.atlassian.plugin.remotable.test.pageobjects.confluence.ConfluenceMacroPage;
import com.atlassian.plugin.remotable.test.pageobjects.confluence.ConfluenceMacroTestSuitePage;
import com.atlassian.plugin.remotable.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.remotable.test.pageobjects.confluence.ConfluencePageMacroPage;
import com.atlassian.plugin.remotable.test.pageobjects.confluence.FixedConfluenceTestedProduct;
import com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.remotable.test.server.RunnerSignedRequestHandler;
import com.atlassian.plugin.remotable.test.server.module.ContextParameter;
import com.atlassian.plugin.remotable.test.server.module.ImagePlaceHolder;
import com.atlassian.plugin.remotable.test.server.module.MacroCategory;
import com.atlassian.plugin.remotable.test.server.module.MacroEditor;
import com.atlassian.plugin.remotable.test.server.module.MacroPageModule;
import com.atlassian.plugin.remotable.test.server.module.MacroParameter;
import com.atlassian.plugin.remotable.test.server.module.RemoteMacroModule;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.atlassian.plugin.remotable.test.HttpUtils.renderHtml;
import static com.atlassian.plugin.remotable.test.Utils.createSignedRequestHandler;
import static com.atlassian.plugin.remotable.test.Utils.loadResourceAsString;
import static com.atlassian.plugin.remotable.test.server.AtlassianConnectAddOnRunner.newMustacheServlet;
import static org.junit.Assert.assertTrue;

public class TestConfluence
{
    public static final String ERROR_PREFIX = "There were errors rendering macro:";

    private static TestedProduct<WebDriverTester> product;
    private static ConfluenceOps confluenceOps;
    private static AtlassianConnectAddOnRunner remotePlugin;
    private static RunnerSignedRequestHandler signedRequestHandler;

    static
    {
        System.setProperty("testedProductClass", FixedConfluenceTestedProduct.class.getName());
    }

    @Rule
    public HtmlDumpRule htmlDump = new HtmlDumpRule(product.getTester().getDriver());

    @BeforeClass
    public static void setupJiraAndStartConnectAddOn() throws Exception
    {
        product = OwnerOfTestedProduct.INSTANCE;
        confluenceOps = new ConfluenceOps(product.getProductInstance().getBaseUrl());
        signedRequestHandler = createSignedRequestHandler("app1");
        remotePlugin = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl(), "app1")
                .addOAuth(signedRequestHandler)
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
                        .editor(MacroEditor.at("/myMacroEditor").height("600").width("600").resource(new MyMacroEditorServlet()))
                        .resource(new MyMacroServlet()))
                .add(RemoteMacroModule.key("app1-image")
                        .name("app1-image")
                        .path("/myImageMacro")
                        .outputType("block")
                        .bodyType("none")
                        .imagePlaceHolder(ImagePlaceHolder.at("/public/sandcastles.jpg"))
                        .resource(new MyImageMacroServlet()))
                .add(RemoteMacroModule.key("app1-slow")
                        .name("app1-slow")
                        .title("Remotable Plugin 1 Slow")
                        .path("/mySlowMacro")
                        .outputType("block")
                        .bodyType("none")
                        .parameters(MacroParameter.name("sleep").title("Sleep").type("int").required("false"))
                        .resource(new MySlowMacroServlet()))
                .add(MacroPageModule.key("app1-page")
                        .name("app1-page")
                        .path("/ap")
                        .outputType("block")
                        .bodyType("none")
                        .resource(newMustacheServlet("iframe.mu")))
                .add(RemoteMacroModule.key("app1-counter")
                        .name("app1-counter")
                        .path("/myCounterMacro")
                        .outputType("block")
                        .bodyType("none")
                        .resource(new MyCounterMacroServlet()))
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stop();
        }
    }

    @After
    public void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }

    @Test
    public void testMacro() throws XmlRpcFault, IOException
    {
        Map pageData = confluenceOps.setPage("ds", "test", loadResourceAsString("confluence/test-page-macros.xhtml"));
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        ConfluenceMacroTestSuitePage page = product.visit(ConfluenceMacroTestSuitePage.class, pageData.get("title"));

        Assert.assertEquals(pageData.get("id"), page.getPageIdFromMacro());
        Assert.assertEquals("some note", page.getBodyNoteFromMacro());
        Assert.assertEquals("sandcastles", page.getImageMacroAlt());

        assertTrue(page.getSlowMacroBody().startsWith("There were errors rendering macro:"));
    }

    @Test
    public void testMacroInComment() throws XmlRpcFault, IOException
    {
        Map pageData = confluenceOps.setPage("ds", "test", loadResourceAsString("confluence/test-page.xhtml"));
        Map commentData = confluenceOps.addComment((String) pageData.get("id"), loadResourceAsString("confluence/test-comment.xhtml"));

        ConfluenceMacroTestSuitePage page = product.visit(ConfluenceMacroTestSuitePage.class, pageData.get("title"));

        Assert.assertEquals(commentData.get("id"), page.getPageIdFromMacroInComment());
    }

    @Test
    public void testAnonymousMacro() throws XmlRpcFault, IOException
    {
        Map pageData = confluenceOps.setAnonymousPage("ds", "test", loadResourceAsString("confluence/test-page.xhtml"));
        ConfluenceMacroTestSuitePage page = product.visit(ConfluenceMacroTestSuitePage.class, pageData.get("title"));
        Assert.assertEquals(pageData.get("id"), page.getPageIdFromMacro());
    }

    @Test
    public void testPageMacro() throws XmlRpcFault, IOException
    {
        Map pageData = confluenceOps.setPage("ds", "test", loadResourceAsString("confluence/test-page-macro.xhtml"));
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        ConfluencePageMacroPage page = product.visit(ConfluencePageMacroPage.class, pageData.get("title"), "app1-page-0");

        Assert.assertEquals("Success", page.getMessage());
        Assert.assertEquals(OAuthUtils.getConsumerKey(), page.getConsumerKey());
    }

    @Test
    public void testPageMacroMultipleImplementations() throws XmlRpcFault, IOException
    {
        Map pageData = confluenceOps.setPage("ds", "test", loadResourceAsString("confluence/test-page-macro-multiple.xhtml"));
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);

        ConfluencePageMacroPage iframe1 = product.visit(ConfluencePageMacroPage.class, pageData.get("title"), "app1-page-0");
        Assert.assertEquals("Success", iframe1.getMessage());
        Assert.assertEquals(OAuthUtils.getConsumerKey(), iframe1.getConsumerKey());

        ConfluencePageMacroPage iframe2 = product.visit(ConfluencePageMacroPage.class, pageData.get("title"), "app1-page-1");

        Assert.assertEquals("Success", iframe2.getMessage());
        Assert.assertEquals(OAuthUtils.getConsumerKey(), iframe2.getConsumerKey());
    }

    @Test
    public void testMacroCacheFlushes() throws Exception
    {
        Map pageData = confluenceOps.setPage("ds", "test", loadResourceAsString("confluence/counter-page.xhtml"));
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        ConfluenceCounterMacroPage page = product.visit(ConfluenceCounterMacroPage.class, pageData.get("title"));
        Assert.assertEquals("1", page.getCounterMacroBody());

        // stays the same on a new visit
        page = product.visit(ConfluenceCounterMacroPage.class, pageData.get("title"));
        Assert.assertEquals("1", page.getCounterMacroBody());

        clearCaches();

        page = product.visit(ConfluenceCounterMacroPage.class, pageData.get("title"));
        Assert.assertEquals("2", page.getCounterMacroBody());
    }

    private static void clearCaches() throws Exception
    {
        final URL url = new URL(product.getProductInstance().getBaseUrl() + "/rest/remotable-plugins/latest/macro/app/app1");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        try
        {
            signedRequestHandler.sign(url.toURI(), "DELETE", null, conn);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
        int code = conn.getResponseCode();
        System.out.println("Reset from " + product.getProductInstance().getBaseUrl() + " returned: " + code);
        conn.disconnect();
    }

    @Ignore("Not sure why, but it times out trying to visit the page")
    @Test
    public void testMacroWithTrickleContent() throws Exception, IOException
    {
        Map pageData = confluenceOps.setPage("ds", "test",
                "<div class=\"trickle-macro\">\n" +
                        "   <ac:macro ac:name=\"trickle\" />\n" +
                        "</div>");

        AtlassianConnectAddOnRunner runner = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl(), "trickle")
                .add(RemoteMacroModule.key("trickle").path("/trickle").resource(new MyTrickleMacroServlet()))
                .start();

        ConfluenceMacroPage page = product.visit(ConfluenceMacroPage.class, pageData.get("title"));

        System.out.println("error: " + page.getMacroError("trickle"));
        assertTrue(page.getMacroError("trickle").startsWith(ERROR_PREFIX));
        runner.stop();
    }

    public static class MyTrickleMacroServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            PrintWriter out = resp.getWriter();
            resp.setContentType("text/html");
            out.write("<div>");
            try
            {
                for (int x = 0; x < 30; x++)
                {
                    System.out.println("===== writing " + x);
                    out.write("<p>" + x + "</p>");
                    out.flush();
                    Thread.sleep(1000);
                }
            }
            catch (InterruptedException e)
            {
                // do nothing
            }
            out.write("</div>");
            out.close();
        }
    }

    public static final class MyMacroServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            final String pageId = req.getParameter("ctx_page_id");
            final String favoriteFooty = req.getParameter("footy");
            final String body = req.getParameter("body");
            resp.setDateHeader("Expires", System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10));
            resp.setHeader("Cache-Control", "public");

            renderHtml(resp, "macro.mu", new HashMap<String, Object>()
            {{
                    put("pageId", pageId);
                    put("favoriteFooty", favoriteFooty);
                    put("body", body);
                    put("server", "???");
                }});
        }
    }

    public static final class MyMacroEditorServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            final Map<String, Object> context = new HashMap<String, Object>();
            context.put("baseUrl", "???");
            renderHtml(resp, "macro-editor.mu", context);
        }
    }

    public static final class MyImageMacroServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            resp.setContentType("text/html");
            PrintWriter writer = resp.getWriter();
            writer.print("<img src=\"/public/sandcastles.jpg\" alt=\"sandcastles\"/>");
            writer.close();
        }
    }

    public static final class MySlowMacroServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            int sleepSeconds = req.getParameter("sleep") != null ? Integer.parseInt(req.getParameter("sleep")) : 22;
            try
            {
                Thread.sleep(sleepSeconds * 1000);
            }
            catch (InterruptedException e)
            {
                // do nothing
            }
            resp.setContentType("text/html");
            resp.getWriter().write("finished");
            resp.getWriter().close();
        }
    }

    public static final class MyCounterMacroServlet extends HttpServlet
    {
        private static final long ONE_YEAR_SECONDS = 60L * 60L * 24L * 365L;
        private static final long ONE_YEAR_MILLISECONDS = 1000 * ONE_YEAR_SECONDS;
        private int counter = 1;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            resp.setContentType("text/html");
            resp.setDateHeader("Expires", System.currentTimeMillis() + ONE_YEAR_MILLISECONDS);
            resp.setHeader("Cache-Control", "s-maxage=" + ONE_YEAR_SECONDS);
            PrintWriter writer = resp.getWriter();
            writer.print("<div class=\"rp-counter\">" + counter++ + "</div>");
            writer.close();
        }
    }
}
