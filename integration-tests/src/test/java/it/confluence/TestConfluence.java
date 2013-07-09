package it.confluence;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.remotable.junit.HtmlDumpRule;
import com.atlassian.plugin.remotable.test.OAuthUtils;
import com.atlassian.plugin.remotable.test.OwnerOfTestedProduct;
import com.atlassian.plugin.remotable.test.RemotePluginRunner;
import com.atlassian.plugin.remotable.test.RemoteWebPanel;
import com.atlassian.plugin.remotable.test.confluence.ConfluenceCounterMacroPage;
import com.atlassian.plugin.remotable.test.confluence.ConfluenceEditPage;
import com.atlassian.plugin.remotable.test.confluence.ConfluenceMacroPage;
import com.atlassian.plugin.remotable.test.confluence.ConfluenceMacroTestSuitePage;
import com.atlassian.plugin.remotable.test.confluence.ConfluenceOps;
import com.atlassian.plugin.remotable.test.confluence.ConfluencePageMacroPage;
import com.atlassian.plugin.remotable.test.confluence.FixedConfluenceTestedProduct;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.junit.After;
import org.junit.Assert;
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
import java.net.MalformedURLException;
import java.util.Map;

import static com.atlassian.plugin.remotable.test.RemotePluginUtils.clearMacroCaches;
import static com.atlassian.plugin.remotable.test.Utils.loadResourceAsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestConfluence
{
    private static final TestedProduct<WebDriverTester> product;
    private static final ConfluenceOps confluenceOps;
    public static final String ERROR_PREFIX = "There were errors rendering macro:";

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
	public void testMacro() throws XmlRpcFault, IOException
    {
        Map pageData = confluenceOps.setPage("ds", "test", loadResourceAsString(
                "confluence/test-page-macros.xhtml"));
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
        Map pageData = confluenceOps.setPage("ds", "test", loadResourceAsString(
                "confluence/test-page.xhtml"));
        Map commentData = confluenceOps.addComment((String) pageData.get("id"),
                loadResourceAsString("confluence/test-comment.xhtml"));

        ConfluenceMacroTestSuitePage page = product.visit(ConfluenceMacroTestSuitePage.class, pageData.get("title"));

        Assert.assertEquals(commentData.get("id"), page.getPageIdFromMacroInComment());
    }
    @Test
    public void testAnonymousMacro() throws XmlRpcFault, IOException
    {
        Map pageData = confluenceOps.setAnonymousPage("ds", "test", loadResourceAsString(
                "confluence/test-page.xhtml"));
        ConfluenceMacroTestSuitePage page = product.visit(ConfluenceMacroTestSuitePage.class, pageData.get("title"));
        Assert.assertEquals(pageData.get("id"), page.getPageIdFromMacro());
    }

    @Test
    public void testPageMacro() throws XmlRpcFault, IOException
    {
        Map pageData = confluenceOps.setPage("ds", "test", loadResourceAsString(
                "confluence/test-page-macro.xhtml"));
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        ConfluencePageMacroPage page = product.visit(ConfluencePageMacroPage.class, pageData.get("title"), "app1-page-0");

        Assert.assertEquals("Success", page.getMessage());
        Assert.assertEquals(OAuthUtils.getConsumerKey(), page.getConsumerKey());
    }

    @Test
    public void testPageMacroMultipleImplementations() throws XmlRpcFault, IOException
    {
        Map pageData = confluenceOps.setPage("ds", "test", loadResourceAsString(
                "confluence/test-page-macro-multiple.xhtml"));
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);

        ConfluencePageMacroPage iframe1 = product.visit(ConfluencePageMacroPage.class, pageData.get("title"), "app1-page-0");
        Assert.assertEquals("Success", iframe1.getMessage());
        Assert.assertEquals(OAuthUtils.getConsumerKey(), iframe1.getConsumerKey());

        ConfluencePageMacroPage iframe2 = product.visit(ConfluencePageMacroPage.class, pageData.get("title"), "app1-page-1");

        Assert.assertEquals("Success", iframe2.getMessage());
        Assert.assertEquals(OAuthUtils.getConsumerKey(), iframe2.getConsumerKey());
    }

    @Test
	public void testMacroCacheFlushes() throws XmlRpcFault, IOException
    {
        Map pageData = confluenceOps.setPage("ds", "test", loadResourceAsString("confluence/counter-page.xhtml"));
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        ConfluenceCounterMacroPage page = product.visit(ConfluenceCounterMacroPage.class, pageData.get("title"));
        Assert.assertEquals("1", page.getCounterMacroBody());

        // stays the same on a new visit
        page = product.visit(ConfluenceCounterMacroPage.class, pageData.get("title"));
        Assert.assertEquals("1", page.getCounterMacroBody());

        clearMacroCaches(product.getProductInstance(), "app1");
        page = product.visit(ConfluenceCounterMacroPage.class, pageData.get("title"));
        Assert.assertEquals("2", page.getCounterMacroBody());
	}

    @Test
    public void testRemotableWebPanelOnEditPage() throws MalformedURLException, XmlRpcFault
    {
        final Map pageData = confluenceOps.setPage("ds", "Page with webpanel", "some page content");
        final String pageId = (String) pageData.get("id");
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        final RemoteWebPanel remoteWebPanel = product.visit(ConfluenceEditPage.class, "remotable-web-panel-confluence-edit-screen-web-panel", pageId).getRemoteWebPanel();
        assertEquals(pageId, remoteWebPanel.getPageId());
        assertEquals("betty", remoteWebPanel.getUserId());
    }

    @Ignore("Not sure why, but it times out trying to visit the page")
    @Test
    public void testMacroWithTrickleContent() throws Exception, IOException
    {
        Map pageData = confluenceOps.setPage("ds", "test",
                "<div class=\"trickle-macro\">\n" +
                "   <ac:macro ac:name=\"trickle\" />\n" +
                "</div>");

        RemotePluginRunner runner = new RemotePluginRunner(product.getProductInstance().getBaseUrl(), "trickle")
                .addMacro("trickle", "/trickle", new MyTrickleMacroServlet())
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
                for (int x=0; x < 30; x++)
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
}
