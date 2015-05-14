package it.confluence;

import com.atlassian.plugin.connect.modules.beans.builder.SingleConditionBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.client.AddOnPropertyClient;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceOps;
import com.atlassian.plugin.connect.test.pageobjects.confluence.ConfluenceViewPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.servlet.ConnectAppServlets;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestAddOnPropertyCondition extends ConfluenceWebDriverTestBase
{
    private static ConnectRunner remotePlugin;
    private AddOnPropertyClient addOnPropertyClient;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        remotePlugin = new ConnectRunner(product)
                .addJWT()
                .addModules(
                        "generalPages",
                        newPageBean()
                                .withName(new I18nProperty("Add-on property controlled page", null))
                                .withKey("add-on-property-page")
                                .withUrl("/pg?page_id={page.id}&page_version={page.version}&page_type={page.type}")
                                .withWeight(1234)
                                .withConditions(new SingleConditionBeanBuilder()
                                        .withCondition("entity_property_equal_to")
                                        .withParam("propertyKey", "prop")
                                        .withParam("entity", "addon")
                                        .withParam("value", "true")
                                        .build())
                                .build())
                .addRoute("/pg", ConnectAppServlets.sizeToParentServlet())
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

    @Before
    public void setUp()
    {
        addOnPropertyClient = new AddOnPropertyClient(product, remotePlugin);
        TestUser user = testUserFactory.basicUser();
        login(user);
    }

    @Test
    public void webPanelShouldBeVisibleIfAddOnPropertyIsSetToTrue() throws Exception
    {
        addOnPropertyClient.putProperty(remotePlugin.getAddon().getKey(), "prop", "true");

        assertThat(webPageIsVisible(), equalTo(true));
    }

    @Test
    public void webPanelShouldNotBeVisibleIfAddOnPropertyIsSetToFalse() throws Exception
    {
        addOnPropertyClient.putProperty(remotePlugin.getAddon().getKey(), "prop", "false");

        assertThat(webPageIsVisible(), equalTo(false));
    }

    @Test
    public void webPanelShouldNotBeVisibleIfAddOnPropertyIsNotSet() throws Exception
    {
        addOnPropertyClient.deleteProperty(remotePlugin.getAddon().getKey(), "prop", "false");

        assertThat(webPageIsVisible(), equalTo(false));
    }

    private boolean webPageIsVisible()
    {
        try
        {
            ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(testUserFactory.basicUser()), "ds", "A test page", "some page content");

            product.visit(ConfluenceViewPage.class, pageData.getId());

            return connectPageOperations.existsWebItem(ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(), "add-on-property-page"));
        }
        catch (Exception ex)
        {
            throw new RuntimeException("ex");
        }
    }
}
