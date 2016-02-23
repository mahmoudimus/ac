package it.common;

import com.atlassian.pageobjects.elements.query.Queries;
import com.atlassian.pageobjects.elements.timeout.DefaultTimeouts;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.SingleConditionBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.client.AddonPropertyClient;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestAddonPropertyCondition extends MultiProductWebDriverTestBase {
    private static final String PAGE_KEY = "add-on-property-page";
    private static final String PAGE_KEY_WITH_OBJECT_NAME = PAGE_KEY + "-with-object-name";

    private static ConnectRunner remotePlugin;

    private AddonPropertyClient addonPropertyClient;

    @BeforeClass
    public static void startConnectAddon() throws Exception {
        final ConnectPageModuleBean pageWithoutObjectName = newPageBean()
                .withName(new I18nProperty(PAGE_KEY, null))
                .withKey(PAGE_KEY)
                .withUrl("/pg")
                .withLocation(getGloballyVisibleLocation())
                .withWeight(1234)
                .withConditions(new SingleConditionBeanBuilder()
                        .withCondition("entity_property_equal_to")
                        .withParam("propertyKey", "prop")
                        .withParam("entity", "addon")
                        .withParam("value", "true")
                        .build())
                .build();

        final ConnectPageModuleBean pageWithObjectName = newPageBean()
                .withName(new I18nProperty(PAGE_KEY_WITH_OBJECT_NAME, null))
                .withKey(PAGE_KEY_WITH_OBJECT_NAME)
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
                .build();

        remotePlugin = new ConnectRunner(product)
                .addJWT()
                .addModules("generalPages", pageWithObjectName, pageWithoutObjectName)
                .addRoute("/pg", ConnectAppServlets.sizeToParentServlet())
                .start();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception {
        if (remotePlugin != null) {
            remotePlugin.stopAndUninstall();
        }
    }

    @Before
    public void setUp() {
        addonPropertyClient = new AddonPropertyClient(product, remotePlugin);
        TestUser user = testUserFactory.basicUser();
        login(user);
    }

    @Test
    public void pageShouldBeVisibleIfAddonPropertyIsSetToTrueWithoutObjectName() throws Exception {
        addonPropertyClient.putProperty(remotePlugin.getAddon().getKey(), "prop", "true");

        waitUntilTrue(Queries.forSupplier(new DefaultTimeouts(), () -> webPageIsVisible(PAGE_KEY)));
    }

    @Test
    public void pageShouldNotBeVisibleIfAddonPropertyIsSetToFalseWithoutObjectName() throws Exception {
        addonPropertyClient.putProperty(remotePlugin.getAddon().getKey(), "prop", "false");

        assertThat(webPageIsVisible(PAGE_KEY), equalTo(false));
    }

    @Test
    public void pageShouldNotBeVisibleIfAddonPropertyIsNotSetWithoutObjectName() throws Exception {
        addonPropertyClient.deleteProperty(remotePlugin.getAddon().getKey(), "prop", "false");

        assertThat(webPageIsVisible(PAGE_KEY), equalTo(false));
    }

    @Test
    public void pageShouldBeVisibleIfAddonPropertyIsSetToTrueWithObjectName() throws Exception {
        addonPropertyClient.putProperty(remotePlugin.getAddon().getKey(), "prop", generateTestData(true).toString());

        waitUntilTrue(Queries.forSupplier(new DefaultTimeouts(), () -> webPageIsVisible(PAGE_KEY_WITH_OBJECT_NAME)));
    }

    @Test
    public void pageShouldNotBeVisibleIfAddonPropertyIsNotSetButSubPropertyMissingWithObjectName() throws Exception {
        addonPropertyClient.putProperty(remotePlugin.getAddon().getKey(), "prop", generateIncorrectTestData(true).toString());

        assertThat(webPageIsVisible(PAGE_KEY_WITH_OBJECT_NAME), equalTo(false));
    }

    @Test
    public void pageShouldNotBeVisibleIfAddonPropertyIsSetToFalseWithObjectName() throws Exception {
        addonPropertyClient.putProperty(remotePlugin.getAddon().getKey(), "prop", generateTestData(false).toString());

        assertThat(webPageIsVisible(PAGE_KEY_WITH_OBJECT_NAME), equalTo(false));
    }

    @Test
    public void pageShouldNotBeVisibleIfAddonPropertyIsNotSetWithObjectName() throws Exception {
        addonPropertyClient.deleteProperty(remotePlugin.getAddon().getKey(), "prop", generateTestData(false).toString());

        assertThat(webPageIsVisible(PAGE_KEY_WITH_OBJECT_NAME), equalTo(false));
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

    private boolean webPageIsVisible(final String expectedKey) {
        loginAndVisit(testUserFactory.admin(), HomePage.class);
        return connectPageOperations().existsWebItem(ModuleKeyUtils.addonAndModuleKey(remotePlugin.getAddon().getKey(), expectedKey));
    }
}
