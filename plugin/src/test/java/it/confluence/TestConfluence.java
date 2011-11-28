package it.confluence;

import com.atlassian.labs.remoteapps.test.OwnerOfTestedProduct;
import com.atlassian.labs.remoteapps.test.confluence.ConfluenceMacroPage;
import com.atlassian.labs.remoteapps.test.confluence.ConfluenceOps;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.junit.After;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import java.io.IOException;
import java.util.Map;

import static com.atlassian.labs.remoteapps.test.Utils.loadResourceAsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestConfluence
{
    private static TestedProduct<?> product = OwnerOfTestedProduct.INSTANCE;
    private static ConfluenceOps confluenceOps = new ConfluenceOps();

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

        assertTrue(page.getSlowMacroBody().startsWith("ERROR"));
	}
}
