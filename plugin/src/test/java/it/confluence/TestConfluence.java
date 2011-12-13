package it.confluence;

import com.atlassian.functest.selenium.internal.ConfluenceTestedProduct;
import com.atlassian.labs.remoteapps.test.OwnerOfTestedProduct;
import com.atlassian.labs.remoteapps.test.confluence.ConfluenceMacroPage;
import com.atlassian.labs.remoteapps.test.confluence.ConfluenceOps;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.AtlassianWebDriverTestBase;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.junit.After;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestConfluence
{
    private static TestedProduct<WebDriverTester> product = OwnerOfTestedProduct.INSTANCE;
    private static ConfluenceOps confluenceOps = new ConfluenceOps();

    private final Logger log = LoggerFactory.getLogger(TestConfluence.class);

    @Rule
    public MethodRule rule = new TestWatchman() {

        private String destinationFolder;

        @Override
        public void starting(final FrameworkMethod method)
        {
            log.info("----- Starting " + method.getName());

            destinationFolder = "target/webdriverTests/" + method.getMethod().getDeclaringClass().getName();

            File dir = new File(destinationFolder);
            // Clean up the directory for the next run
            if (dir.exists()) {
                dir.delete();
            }

            dir.mkdirs();
        }

        @Override
        public void succeeded(final FrameworkMethod method)
        {
            log.info("----- Succeeded " + method.getName());
        }

        @Override
        public void failed(final Throwable e, final FrameworkMethod method)
        {
            final AtlassianWebDriver driver = product.getTester().getDriver();
            String baseFileName =  destinationFolder + "/" + method.getName();
            File dumpFile = new File(baseFileName + ".html");
            log.error(e.getMessage(), e);
            log.info("----- Test Failed. " + e.getMessage());

            log.info("----- At page: " + driver.getCurrentUrl());
            log.info("----- Dumping page source to: " + dumpFile.getAbsolutePath());

            // Take a screen shot and dump it.
            driver.dumpSourceTo(dumpFile);
            driver.takeScreenshotTo(new File(baseFileName + ".png"));

        }

        @Override
        public void finished(final FrameworkMethod method)
        {
            log.info("----- Finished " + method.getName());
        }
    };

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
}
