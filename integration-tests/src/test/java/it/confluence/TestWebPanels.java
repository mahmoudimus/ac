package it.confluence;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.remotable.junit.HtmlDumpRule;
import com.atlassian.plugin.remotable.test.OwnerOfTestedProduct;
import com.atlassian.plugin.remotable.test.RemoteWebPanel;
import com.atlassian.plugin.remotable.test.RemoteWebPanels;
import com.atlassian.plugin.remotable.test.confluence.ConfluenceEditPage;
import com.atlassian.plugin.remotable.test.confluence.ConfluenceOps;
import com.atlassian.plugin.remotable.test.confluence.FixedConfluenceTestedProduct;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import org.junit.Rule;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import java.net.MalformedURLException;
import java.util.Map;

import static it.TestConstants.BETTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test of remote web panels in Confluence.
 */
public class TestWebPanels
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

    @Test
    public void testRemoteWebPanelOnEditPage() throws MalformedURLException, XmlRpcFault
    {
        final Map pageData = confluenceOps.setPage("ds", "Page with webpanel", "some page content");
        final String pageId = (String) pageData.get("id");
        product.visit(LoginPage.class).login(BETTY, BETTY, HomePage.class);
        ConfluenceEditPage editPage = product.visit(ConfluenceEditPage.class, pageId);
        RemoteWebPanels webPanels = editPage.getWebPanels();
        assertNotNull("Remote web panels should be found", webPanels);
        final RemoteWebPanel webPanel = webPanels.getWebPanel("edit-screen-web-panel");
        assertNotNull("Panel should be found", webPanel);
        assertEquals(pageId, webPanel.getPageId());
        assertEquals(BETTY, webPanel.getUserId());
    }
}
