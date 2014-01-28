package it.capabilities.confluence;

import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceEditPage;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Optional;
import it.capabilities.CheckUsernameConditionServlet;
import it.confluence.ConfluenceWebDriverTestBase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean.newCompositeConditionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static it.TestConstants.BARNEY_USERNAME;
import static it.TestConstants.BETTY_USERNAME;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestConfluenceConditions extends ConfluenceWebDriverTestBase
{
    private static ConnectRunner remotePlugin;
    private static final String ONLY_BETTY_WEBITEM = "only-betty";
    private static final String ONLY_BARNEY_WEBITEM = "only-barney";
    private static final String BETTY_AND_BARNEY_WEBITEM = "betty-and-barney";
    private static final String ADMIN_RIGHTS_WEBITEM = "admin-rights";

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(),"my-plugin")
                .addModules("webItems", newWebItemBean()
                        .withName(new I18nProperty("Only Betty", ONLY_BETTY_WEBITEM))
                        .withKey("only-betty")
                        .withLocation("system.browse")
                        .withWeight(1)
                        .withUrl("http://www.google.com")
                        .withConditions(
                                newSingleConditionBean().withCondition("user_is_logged_in").build()
                                , newSingleConditionBean().withCondition("/onlyBettyCondition").build()
                        )
                        .build()
                        , newWebItemBean()
                        .withName(new I18nProperty("Betty And Barney", BETTY_AND_BARNEY_WEBITEM))
                        .withKey("betty-and-barney")
                        .withLocation("system.browse")
                        .withWeight(1)
                        .withUrl("http://www.google.com")
                        .withConditions(
                                newSingleConditionBean().withCondition("user_is_logged_in").build()
                                , newCompositeConditionBean()
                                .withType(CompositeConditionType.OR)
                                .withConditions(
                                        newSingleConditionBean().withCondition("/onlyBettyCondition").build()
                                        , newSingleConditionBean().withCondition("/onlyBarneyCondition").build()
                                ).build()
                        )
                        .build()
                        , newWebItemBean()
                        .withName(new I18nProperty("Admin Rights", ADMIN_RIGHTS_WEBITEM))
                        .withKey("admin-rights")
                        .withLocation("system.browse")
                        .withWeight(1)
                        .withUrl("http://www.google.com")
                        .withConditions(
                                newSingleConditionBean().withCondition("user_is_confluence_administrator").build()
                        )
                        .build())
                .addRoute("/onlyBarneyCondition", new CheckUsernameConditionServlet(BARNEY_USERNAME))
                .addRoute("/onlyBettyCondition", new CheckUsernameConditionServlet(BETTY_USERNAME))
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void bettyCanSeeBettyWebItem() throws Exception
    {
        loginAsBetty();

        ConfluenceEditPage editPage = visitEditPage();
        RemoteWebItem webItem = editPage.findWebItem(ONLY_BETTY_WEBITEM, Optional.of("help-menu-link"));
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void barneyCannotSeeBettyWebItem() throws Exception
    {
        loginAsBarney();

        ConfluenceEditPage editPage = visitEditPage();
        assertTrue("Web item should NOT be found", editPage.webItemDoesNotExist(ONLY_BETTY_WEBITEM));
    }

    @Test
    public void adminCannotSeeBettyWebItem() throws Exception
    {
        loginAsAdmin();

        ConfluenceEditPage editPage = visitEditPage();
        assertTrue("Web item should NOT be found", editPage.webItemDoesNotExist(ONLY_BETTY_WEBITEM));
    }

    @Test
    public void bettyCanSeeBettyAndBarneyWebItem() throws Exception
    {
        loginAsBetty();

        ConfluenceEditPage editPage = visitEditPage();
        RemoteWebItem webItem = editPage.findWebItem(BETTY_AND_BARNEY_WEBITEM, Optional.of("help-menu-link"));
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void barneyCanSeeBettyAndBarneyWebItem() throws Exception
    {
        loginAsBarney();

        ConfluenceEditPage editPage = visitEditPage();
        RemoteWebItem webItem = editPage.findWebItem(BETTY_AND_BARNEY_WEBITEM, Optional.of("help-menu-link"));
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void adminCannotSeeBettyAndBarneyWebItem() throws Exception
    {
        loginAsAdmin();

        ConfluenceEditPage editPage = visitEditPage();
        assertTrue("Web item should NOT be found", editPage.webItemDoesNotExist(BETTY_AND_BARNEY_WEBITEM));
    }

    @Test
    public void bettyCanSeeAdminRightsWebItem() throws Exception
    {
        loginAsBetty();

        ConfluenceEditPage editPage = visitEditPage();
        RemoteWebItem webItem = editPage.findWebItem(ADMIN_RIGHTS_WEBITEM, Optional.of("help-menu-link"));
        assertNotNull("Web item should be found", webItem);
    }

    @Test
    public void barneyCannotSeeAdminRightsWebItem() throws Exception
    {
        loginAsBarney();

        ConfluenceEditPage editPage = visitEditPage();
        assertTrue("Web item should NOT be found", editPage.webItemDoesNotExist(ADMIN_RIGHTS_WEBITEM));
    }

    @Test
    public void adminCanSeeAdminRightsWebItem() throws Exception
    {
        loginAsAdmin();

        ConfluenceEditPage editPage = visitEditPage();
        RemoteWebItem webItem = editPage.findWebItem(ADMIN_RIGHTS_WEBITEM, Optional.of("help-menu-link"));
        assertNotNull("Web item should be found", webItem);
    }
    
    private ConfluenceEditPage visitEditPage() throws Exception
    {
        final ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(new ConfluenceOps.ConfluenceUser("admin", "admin")), "ds", "Page with webpanel", "some page content");
        final String pageId = pageData.getId();
        return product.visit(ConfluenceEditPage.class, pageId);
    }
                
}
