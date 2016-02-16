package it.jira.condition;

import java.util.Map;
import java.util.Optional;

import com.atlassian.connect.test.jira.pageobjects.ViewIssuePageWithAddonFragments;
import com.atlassian.elasticsearch.shaded.google.common.base.Joiner;
import com.atlassian.fugue.Pair;
import com.atlassian.plugin.connect.modules.beans.AddonUrlContext;
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

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static com.atlassian.plugin.connect.test.common.servlet.condition.ParameterCapturingConditionServlet.PARAMETER_CAPTURE_URL;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestJiraInlineConditions extends AbstractJiraConditionsTest
{
    private static ConnectRunner runner;

    private static final String WEB_PANEL_CONTENT_URL = "/web-panel";

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();

    private TestUser user;
    private String issueKey;

    @BeforeClass
    public static void startConnectAddon() throws Exception
    {
        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddonKey())
                .setAuthenticationToNone()
                .addModules("webItems", webItems())
                .addModules("webPanels", webPanels())
                .addRoute(PARAMETER_CAPTURE_URL, PARAMETER_CAPTURING_SERVLET)
                .addRoute(WEB_PANEL_CONTENT_URL, ConnectAppServlets.customMessageServlet("Whatever"))
                .start();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception
    {
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
    }

    @Before
    public void setUp()
    {
        user = testUserFactory.admin();
        issueKey = createIssueSatisfyingAllConditions(user);
    }

    @Test
    public void inlineConditionInWebItemsShouldEvaluateToTrue()
    {
        ViewIssuePageWithAddonFragments viewIssuePage = loginAndVisit(user, ViewIssuePageWithAddonFragments.class, issueKey);

        CONDITION_NAMES.forEach(nameParams -> {
            String moduleKey = addonAndModuleKey(runner.getAddon().getKey(), webItemKey(nameParams));
            RemoteWebItem webItem = viewIssuePage.findWebItem(moduleKey, Optional.<String>empty());
            webItem.click();
            RemoteDialog dialogPage = product.getPageBinder().bind(RemoteDialog.class);
            dialogPage.submitAndWaitUntilHidden();

            Map<String, String> conditionParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();
            assertThat(conditionParams, hasEntry(equalTo("condition"), equalTo("true")));
        });
    }

    @Test
    public void inlineConditionInWebPanelsShouldEvaluateToTrue()
    {
        ViewIssuePageWithAddonFragments viewIssuePage = loginAndVisit(user, ViewIssuePageWithAddonFragments.class, issueKey);

        CONDITION_NAMES.forEach(nameParams -> {
            String panelId = addonAndModuleKey(runner.getAddon().getKey(), webPanelKey(nameParams.left()));
            RemoteWebPanel webPanel = viewIssuePage.findWebPanel(panelId);
            assertTrue(Boolean.valueOf(webPanel.getFromQueryString("condition")));
        });
    }

    @Test
    public void unrecognizedConditionsAreNotResolved()
    {
        ViewIssuePageWithAddonFragments viewIssuePage = loginAndVisit(user, ViewIssuePageWithAddonFragments.class, issueKey);
        String panelId = addonAndModuleKey(runner.getAddon().getKey(), webPanelKey(CONDITION_NAMES.get(0).left()));
        RemoteWebPanel webPanel = viewIssuePage.findWebPanel(panelId);
        assertThat(webPanel.getFromQueryString("invalidCondition"), equalTo(""));
    }

    private static WebItemModuleBean[] webItems()
    {
        return CONDITION_NAMES.stream().map(nameParams -> newWebItemBean()
                .withName(new I18nProperty(webItemKey(nameParams), webItemKey(nameParams)))
                .withKey(webItemKey(nameParams))
                .withContext(AddonUrlContext.addon)
                .withLocation("operations-operations") // issue operations
                .withWeight(1)
                .withTarget(new WebItemTargetBeanBuilder().withType(WebItemTargetType.dialog).build())
                .withUrl(PARAMETER_CAPTURE_URL + "?condition={" + conditionVariable(nameParams) + "}&invalidCondition={condition.invalid}")
                .build()).collect(toList()).toArray(new WebItemModuleBean[CONDITION_NAMES.size()]);
    }

    private static WebPanelModuleBean[] webPanels()
    {
        return CONDITION_NAMES.stream().map(nameParams -> new WebPanelModuleBeanBuilder()
                .withName(new I18nProperty(webPanelKey(nameParams.left()), webPanelKey(nameParams.left())))
                .withKey(webPanelKey(nameParams.left()))
                .withLocation("atl.jira.view.issue.right.context")
                .withWeight(1)
                .withUrl(PARAMETER_CAPTURE_URL + "?condition={" + conditionVariable(nameParams) + "}&invalidCondition={condition.invalid}")
                .build()).collect(toList()).toArray(new WebPanelModuleBean[CONDITION_NAMES.size()]);
    }

    private static String webItemKey(final Pair<String, Map<String, String>> condition)
    {
        return "test-web-item-" + condition.left().replace("_", "-") + "-" + condition.right().hashCode();
    }

    private static String webPanelKey(final String name)
    {
        return "test-web-panel-" + name.replace("_", "-");
    }

    private static String conditionVariable(Pair<String, Map<String, String>> nameParams)
    {
        Map<String, String> parameters = nameParams.right();
        String params = parameters.isEmpty() ? "" :
                "(" + Joiner.on(",").withKeyValueSeparator("=").join(parameters.entrySet()) + ")";

        return String.format("condition.%s%s", nameParams.left(), params);
    }
}
