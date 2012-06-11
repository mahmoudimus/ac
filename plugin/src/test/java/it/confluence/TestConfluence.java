package it.confluence;

import com.atlassian.labs.remoteapps.test.HtmlDumpRule;
import com.atlassian.labs.remoteapps.test.OAuthUtils;
import com.atlassian.labs.remoteapps.test.OwnerOfTestedProduct;
import com.atlassian.labs.remoteapps.test.confluence.*;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import redstone.xmlrpc.XmlRpcFault;

import java.io.IOException;
import java.util.Map;

import static com.atlassian.labs.remoteapps.test.RemoteAppUtils.clearMacroCaches;
import static com.atlassian.labs.remoteapps.test.Utils.loadResourceAsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestConfluence
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
    public MethodRule rule = new HtmlDumpRule(product.getTester().getDriver());

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
        ConfluenceMacroPage page = product.visit(ConfluenceMacroPage.class, pageData.get("title"));

        assertEquals(pageData.get("id"), page.getPageIdFromMacro());
        assertEquals("some note", page.getBodyNoteFromMacro());
        assertEquals("sandcastles", page.getImageMacroAlt());

        assertTrue(page.getSlowMacroBody().startsWith("ERROR"));
	}

    @Test
    public void testMacroInComment() throws XmlRpcFault, IOException
    {
        Map pageData = confluenceOps.setPage("ds", "test", loadResourceAsString(
                "confluence/test-page.xhtml"));
        Map commentData = confluenceOps.addComment((String) pageData.get("id"),
                loadResourceAsString("confluence/test-comment.xhtml"));

        ConfluenceMacroPage page = product.visit(ConfluenceMacroPage.class, pageData.get("title"));

        assertEquals(commentData.get("id"), page.getPageIdFromMacroInComment());
    }
    @Test
    public void testAnonymousMacro() throws XmlRpcFault, IOException
    {
        Map pageData = confluenceOps.setAnonymousPage("ds", "test", loadResourceAsString(
                "confluence/test-page.xhtml"));
        ConfluenceMacroPage page = product.visit(ConfluenceMacroPage.class, pageData.get("title"));
        assertEquals(pageData.get("id"), page.getPageIdFromMacro());
    }

    @Test
    public void testPageMacro() throws XmlRpcFault, IOException
    {
        Map pageData = confluenceOps.setPage("ds", "test", loadResourceAsString(
                "confluence/test-page-macro.xhtml"));
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        ConfluencePageMacroPage page = product.visit(ConfluencePageMacroPage.class, pageData.get("title"), "app1-page-0");

        assertEquals("Success", page.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), page.getConsumerKey());
    }

    @Test
    public void testPageMacroMultipleImplementations() throws XmlRpcFault, IOException
    {
        Map pageData = confluenceOps.setPage("ds", "test", loadResourceAsString(
                "confluence/test-page-macro-multiple.xhtml"));
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);

        ConfluencePageMacroPage iframe1 = product.visit(ConfluencePageMacroPage.class, pageData.get("title"), "app1-page-0");
        assertEquals("Success", iframe1.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), iframe1.getConsumerKey());

        ConfluencePageMacroPage iframe2 = product.visit(ConfluencePageMacroPage.class, pageData.get("title"), "app1-page-1");

        assertEquals("Success", iframe2.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), iframe2.getConsumerKey());
    }

    @Test
	public void testContextParam() throws XmlRpcFault, IOException
    {
        Map pageData = confluenceOps.setPage("ds", "test", loadResourceAsString(
                "confluence/test-page.xhtml"));
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        Map<String,String> params = product.visit(ConfluenceMacroPage.class, pageData.get("title"))
                                          .visitGeneralLink()
                                          .getIframeQueryParams();

        assertEquals(pageData.get("id"), params.get("page_id"));
	}

    @Test
	public void testMacroCacheFlushes() throws XmlRpcFault, IOException
    {
        Map pageData = confluenceOps.setPage("ds", "test", loadResourceAsString(
                "confluence/counter-page.xhtml"));
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        ConfluenceCounterMacroPage page = product.visit(ConfluenceCounterMacroPage.class, pageData.get("title"));
        assertEquals("1", page.getCounterMacroBody());

        // stays the same on a new visit
        page = product.visit(ConfluenceCounterMacroPage.class, pageData.get("title"));
        assertEquals("1", page.getCounterMacroBody());

        clearMacroCaches(product.getProductInstance(), "app1");
        page = product.visit(ConfluenceCounterMacroPage.class, pageData.get("title"));
        assertEquals("2", page.getCounterMacroBody());
	}
}
