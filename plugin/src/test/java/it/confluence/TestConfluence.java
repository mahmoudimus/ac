package it.confluence;

import com.atlassian.functest.selenium.internal.ConfluenceTestedProduct;
import com.atlassian.labs.remoteapps.test.HtmlDumpRule;
import com.atlassian.labs.remoteapps.test.OwnerOfTestedProduct;
import com.atlassian.labs.remoteapps.test.confluence.ConfluenceCounterMacroPage;
import com.atlassian.labs.remoteapps.test.confluence.ConfluenceMacroPage;
import com.atlassian.labs.remoteapps.test.confluence.ConfluenceOps;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.AtlassianWebDriverTestBase;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redstone.xmlrpc.XmlRpcFault;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.atlassian.labs.remoteapps.test.Utils.loadResourceAsString;
import static com.atlassian.labs.remoteapps.test.WebHookUtils.waitForEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestConfluence
{
    private static TestedProduct<WebDriverTester> product = TestedProductFactory.create(com.atlassian.webdriver.confluence.ConfluenceTestedProduct.class);
    private static ConfluenceOps confluenceOps = new ConfluenceOps();

    private final Logger log = LoggerFactory.getLogger(TestConfluence.class);

    @Rule
    public MethodRule rule = new HtmlDumpRule(product.getTester().getDriver());

    @After
    public void logout()
    {
        ((WebDriverTester)product.getTester()).getDriver().manage().deleteAllCookies();
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

        confluenceOps.resetMacrosOnPage(product.getProductInstance(), (String) pageData.get("id"));
        page = product.visit(ConfluenceCounterMacroPage.class, pageData.get("title"));
        assertEquals("2", page.getCounterMacroBody());

        confluenceOps.resetMacrosForPlugin(product.getProductInstance(), "app1");
        page = product.visit(ConfluenceCounterMacroPage.class, pageData.get("title"));
        assertEquals("3", page.getCounterMacroBody());
	}

    @Test
	public void testPageCreatedWebHookFired() throws IOException, JSONException, InterruptedException, XmlRpcFault
    {
        String content = "<h1>Love me</h1>";
        Map pageData = confluenceOps.setPage(product.getProductInstance(), "ds", "test", content);

        JSONObject event = null;
        for (int x=0; x<5; x++)
        {
            event = waitForEvent(product.getProductInstance(), "page_created");
            if (pageData.get("id").equals(event.getString("pageId")))
            {
                break;
            }
        }
        assertEquals(pageData.get("creator"), event.getString("author"));
        assertEquals(content, event.getString("content"));
	}
}
