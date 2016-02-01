package it.common;

import java.util.UUID;

import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.timeout.DefaultTimeouts;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.builder.SingleConditionBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.client.AddonPropertyClient;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.TestUser;

import com.google.common.base.Supplier;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestAddonPropertyCondition extends MultiProductWebDriverTestBase
{
    private static final String PAGE_KEY = "add-on-property-page";
    private static final String PAGE_NAME = "Prop";

    private static ConnectRunner remotePlugin;

    private AddonPropertyClient addonPropertyClient;

    @BeforeClass
    public static void startConnectAddon() throws Exception
    {
        remotePlugin = new ConnectRunner(product)
                .addJWT()
                .addModules(
                        "generalPages",
                        newPageBean()
                                .withName(new I18nProperty(PAGE_NAME, null))
                                .withKey(PAGE_KEY)
                                .withUrl("/pg")
                                .withLocation(getGloballyVisibleLocation())
                                .withWeight(1234)
                                .withConditions(new SingleConditionBeanBuilder()
                                        .withCondition("entity_property_equal_to")
                                        .withParam("propertyKey", "prop")
                                        .withParam("entity", "addon")
                                        .withParam("objectName", "level-one.level-two")
                                        .withParam("value", "true")
                                        .build())
                                .build())
                .addRoute("/pg", ConnectAppServlets.sizeToParentServlet())
                .start();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Before
    public void setUp()
    {
        addonPropertyClient = new AddonPropertyClient(product, remotePlugin);
        TestUser user = testUserFactory.basicUser();
        login(user);
    }

    @Test
    public void pageShouldBeVisibleIfAddonPropertyIsSetToTrue() throws Exception
    {
        addonPropertyClient.putProperty(remotePlugin.getAddon().getKey(), "prop", generateTestData(true).toString());

        waitUntilTrue(Queries.forSupplier(new DefaultTimeouts(), new Supplier<Boolean>()
        {
            @Override
            public Boolean get()
            {
                return webPageIsVisible();
            }
        }));
    }

    @Test
    public void pageShouldNotBeVisibleIfAddonPropertyIsNotSetButSubPropertyMissing() throws Exception
    {
        addonPropertyClient.putProperty(remotePlugin.getAddon().getKey(), "prop", generateIncorrectTestData(true).toString());

        assertThat(webPageIsVisible(), equalTo(false));
    }

    @Test
    public void pageShouldNotBeVisibleIfAddonPropertyIsSetToFalse() throws Exception
    {
        addonPropertyClient.putProperty(remotePlugin.getAddon().getKey(), "prop", generateTestData(false).toString());

        assertThat(webPageIsVisible(), equalTo(false));
    }

    @Test
    public void pageShouldNotBeVisibleIfAddonPropertyIsNotSet() throws Exception
    {
        addonPropertyClient.deleteProperty(remotePlugin.getAddon().getKey(), "prop", generateTestData(false).toString());

        assertThat(webPageIsVisible(), equalTo(false));
    }

    private static JsonNode generateTestData(boolean data) {
        final ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.put("random-data", UUID.randomUUID().toString());

        final ObjectNode one = root.putObject("level-one");
        one.put("level-two", data);
        one.put("more-random-data", UUID.randomUUID().toString());

        return root;
    }

    private static JsonNode generateIncorrectTestData(boolean data) {
        final ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.put("random-data", UUID.randomUUID().toString());

        final ObjectNode one = root.putObject("level-one");
        one.put("level-three", data);
        one.put("more-random-data", UUID.randomUUID().toString());

        return root;
    }

    private boolean webPageIsVisible()
    {
        loginAndVisit(testUserFactory.admin(), HomePage.class);
        return connectPageOperations().existsWebItem(ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(), PAGE_KEY));
    }
}
