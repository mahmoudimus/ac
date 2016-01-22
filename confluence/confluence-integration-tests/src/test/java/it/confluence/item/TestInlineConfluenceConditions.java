package it.confluence.item;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.Optional;

import com.atlassian.connect.test.confluence.pageobjects.ConfluenceOps;
import com.atlassian.connect.test.confluence.pageobjects.ConfluenceViewPage;
import com.atlassian.elasticsearch.shaded.google.common.base.Joiner;
import com.atlassian.elasticsearch.shaded.google.common.collect.ImmutableSet;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.WebItemTargetBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.WebPanelModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteDialog;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebPanel;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.condition.ParameterCapturingConditionServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import redstone.xmlrpc.XmlRpcFault;

import static com.atlassian.fugue.Option.some;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static com.atlassian.plugin.connect.test.common.servlet.condition.ParameterCapturingConditionServlet.PARAMETER_CAPTURE_URL;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestInlineConfluenceConditions extends AbstractConfluenceConditionsTest
{
    private static final String WEB_PANEL_CONTENT_URL = "/web-panel";
    private static final String SPACE = "ds";

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();

    private static final ImmutableSet<String> EXCLUDED_CONDITIONS = ImmutableSet.of("tiny_url_supported", "viewing_content");

    private static ConnectRunner addon;
    private TestUser admin;

    @BeforeClass
    public static void startAddon() throws Exception
    {

        addon = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddonKey())
                .setAuthenticationToNone()
                .addRoute(PARAMETER_CAPTURE_URL, PARAMETER_CAPTURING_SERVLET)
                .addRoute(WEB_PANEL_CONTENT_URL, ConnectAppServlets.customMessageServlet("Whatever"));

        for (String conditionName : CONDITION_NAMES)
        {
            addon.addModules("webItems", webItem(conditionName));
            addon.addModules("webPanels", webPanel(conditionName));
        }

        addon.start();
    }

    @AfterClass
    public static void stopAddon() throws Exception
    {
        if (addon != null)
        {
            addon.stopAndUninstall();
        }
    }

    @Before
    public void setUp()
    {
        this.admin = testUserFactory.admin();
    }

    @Test
    public void inlineConditionInWebItemsShouldEvaluateToTrue() throws MalformedURLException, XmlRpcFault
    {
        login(admin);
        createAndVisitViewPage();

        CONDITION_NAMES.forEach(name -> {
            RemoteWebItem webItem = confluencePageOperations.findWebItem(getModuleKey(webItemKey(name)), Optional.<String>empty());
            webItem.click();
            RemoteDialog dialogPage = product.getPageBinder().bind(RemoteDialog.class);
            dialogPage.submitAndWaitUntilHidden();

            Map<String, String> conditionParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();
            assertThat(conditionParams, hasEntry(equalTo("condition"), equalTo(expectedValue(name))));
        });
    }

    @Test
    public void inlineConditionInWebPanelsShouldEvaluateToTrue() throws MalformedURLException, XmlRpcFault
    {
        login(admin);
        createAndVisitViewPage();

        CONDITION_NAMES.forEach(name -> {
            RemoteWebPanel webPanel = confluencePageOperations.findWebPanel(getModuleKey(webPanelKey(name)));
            assertThat(webPanel.getFromQueryString("condition"), equalTo(expectedValue(name)));
        });
    }

    private String expectedValue(String conditionName)
    {
        return EXCLUDED_CONDITIONS.contains(conditionName) ? "" : "true";
    }

    private void createAndVisitViewPage() throws MalformedURLException, XmlRpcFault
    {
        ConfluenceOps.ConfluencePageData pageData = confluenceOps.setPage(some(testUserFactory.admin()), SPACE, "Page with webitem", "some page content");
        product.visit(ConfluenceViewPage.class, pageData.getId());
    }

    private static WebItemModuleBean webItem(String conditionName)
    {
        return newWebItemBean()
                .withKey(webItemKey(conditionName))
                .withUrl(PARAMETER_CAPTURE_URL + "?condition={" + conditionVariable(conditionName) + "}")
                .withLocation("system.content.action")
                .withWeight(CONDITION_NAMES.indexOf(conditionName))
                .withName(new I18nProperty(webItemKey(conditionName), null))
                .withTarget(new WebItemTargetBeanBuilder().withType(WebItemTargetType.dialog).build())
                .build();
    }

    private static WebPanelModuleBean webPanel(String name)
    {
        return new WebPanelModuleBeanBuilder()
                .withName(new I18nProperty(webPanelKey(name), webPanelKey(name)))
                .withKey(webPanelKey(name))
                .withLocation("atl.general")
                .withWeight(1)
                .withUrl(WEB_PANEL_CONTENT_URL + "?condition={" + conditionVariable(name) + "}")
                .build();
    }

    private static String webItemKey(final String name)
    {
        return "test-web-item-" + name.replace("_", "-");
    }

    private static String webPanelKey(final String name)
    {
        return "test-web-panel-" + name.replace("_", "-");
    }

    private static String conditionVariable(String conditionName)
    {
        Map<String, String> parameters = CONDITION_PARAMETERS.get(conditionName);
        String params = parameters == null ? "" :
                "(" + Joiner.on(",").withKeyValueSeparator("=").join(parameters.entrySet()) + ")";

        return String.format("condition.%s%s", conditionName, params);
    }

    private String getModuleKey(String module)
    {
        return addonAndModuleKey(addon.getAddon().getKey(), module);
    }
}
