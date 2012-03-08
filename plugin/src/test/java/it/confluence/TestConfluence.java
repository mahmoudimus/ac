package it.confluence;

import com.atlassian.labs.remoteapps.test.HtmlDumpRule;
import com.atlassian.labs.remoteapps.test.OAuthUtils;
import com.atlassian.labs.remoteapps.test.OwnerOfTestedProduct;
import com.atlassian.labs.remoteapps.test.confluence.ConfluenceCounterMacroPage;
import com.atlassian.labs.remoteapps.test.confluence.ConfluenceMacroPage;
import com.atlassian.labs.remoteapps.test.confluence.ConfluenceOps;
import com.atlassian.labs.remoteapps.test.confluence.ConfluencePageMacroPage;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import redstone.xmlrpc.XmlRpcFault;

import java.io.IOException;
import java.util.Map;

import static com.atlassian.labs.remoteapps.test.RemoteAppUtils.clearMacroCaches;
import static com.atlassian.labs.remoteapps.test.RemoteAppUtils.waitForEvent;
import static com.atlassian.labs.remoteapps.test.Utils.loadResourceAsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestConfluence
{
    private static TestedProduct<WebDriverTester> product = OwnerOfTestedProduct.INSTANCE;
    private static ConfluenceOps confluenceOps = new ConfluenceOps();

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
        Map pageData = confluenceOps.setPage(product.getProductInstance(), "ds", "test", loadResourceAsString(
                "confluence/test-page.xhtml"));
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        ConfluenceMacroPage page = product.visit(ConfluenceMacroPage.class, pageData.get("title"));

        assertEquals(pageData.get("id"), page.getPageIdFromMacro());
        assertEquals("some note", page.getBodyNoteFromMacro());
        assertEquals("sandcastles", page.getImageMacroAlt());

        assertTrue(page.getSlowMacroBody().startsWith("ERROR"));
	}

    @Test
    public void testPageMacro() throws XmlRpcFault, IOException
    {
        Map pageData = confluenceOps.setPage(product.getProductInstance(), "ds", "test", loadResourceAsString(
                "confluence/test-page-macro.xhtml"));
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        ConfluencePageMacroPage page = product.visit(ConfluencePageMacroPage.class, pageData.get("title"), "app1-page-0");

        assertEquals("Success", page.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), page.getConsumerKey());
    }

    @Test
	public void testContextParam() throws XmlRpcFault, IOException
    {
        Map pageData = confluenceOps.setPage(product.getProductInstance(), "ds", "test", loadResourceAsString(
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
        Map pageData = confluenceOps.setPage(product.getProductInstance(), "ds", "test", loadResourceAsString(
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

    @Test
    public void testSearchPerformedWebHookFired() throws XmlRpcFault, IOException, InterruptedException, JSONException
    {
        final String testQuery = "test";
        int results = confluenceOps.search(product.getProductInstance(), testQuery);

        JSONObject event = waitForEvent(product.getProductInstance(), "search_performed");
        assertEquals(testQuery, event.getString("query"));
        assertEquals(results, event.getInt("results"));
    }

    @Test
	public void testPageCreatedWebHookFired() throws IOException, JSONException, InterruptedException, XmlRpcFault
    {
        String content = "<h1>Love me</h1>";
        Map pageData = confluenceOps.setPage(product.getProductInstance(), "ds", "test", content);


        JSONObject page = null;
        for (int x=0; x<5; x++)
        {
            JSONObject event = waitForEvent(product.getProductInstance(), "page_created");
            page = event.getJSONObject("page");
            if (pageData.get("id").equals(page.getString("id")))
            {
                break;
            }
        }
        assertEquals(pageData.get("creator"), page.getString("creatorName"));
	}
}
