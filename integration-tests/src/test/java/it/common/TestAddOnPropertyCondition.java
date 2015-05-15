package it.common;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.builder.SingleConditionBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.client.AddOnPropertyClient;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginAwarePage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import it.servlet.ConnectAppServlets;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestAddOnPropertyCondition extends MultiProductWebDriverTestBase
{
    private static ConnectRunner remotePlugin;
    private AddOnPropertyClient addOnPropertyClient;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        final String productContextPath = product.getProductInstance().getContextPath().toLowerCase();
        String globallyVisibleLocation = productContextPath.contains("jira")
                ? "system.top.navigation.bar"
                : productContextPath.contains("wiki") || productContextPath.contains("confluence")
                        ? "system.help/pages"
                        : null;

        remotePlugin = new ConnectRunner(product)
                .addJWT()
                .addModules(
                        "generalPages",
                        newPageBean()
                                .withName(new I18nProperty("Add-on property controlled page", null))
                                .withKey("add-on-property-page")
                                .withUrl("/pg")
                                .withLocation(globallyVisibleLocation)
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
    public void pageShouldBeVisibleIfAddOnPropertyIsSetToTrue() throws Exception
    {
        addOnPropertyClient.putProperty(remotePlugin.getAddon().getKey(), "prop", "true");

        assertThat(webPageIsVisible(), equalTo(true));
    }

    @Test
    public void pageShouldNotBeVisibleIfAddOnPropertyIsSetToFalse() throws Exception
    {
        addOnPropertyClient.putProperty(remotePlugin.getAddon().getKey(), "prop", "false");

        assertThat(webPageIsVisible(), equalTo(false));
    }

    @Test
    public void pageShouldNotBeVisibleIfAddOnPropertyIsNotSet() throws Exception
    {
        addOnPropertyClient.deleteProperty(remotePlugin.getAddon().getKey(), "prop", "false");

        assertThat(webPageIsVisible(), equalTo(false));
    }

    private boolean webPageIsVisible()
    {
        loginAndVisit(testUserFactory.admin(), HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "add-on-property-page", "Add-on property controlled page", remotePlugin.getAddon().getKey());
        return page.isRemotePluginLinkPresent();
    }
}
