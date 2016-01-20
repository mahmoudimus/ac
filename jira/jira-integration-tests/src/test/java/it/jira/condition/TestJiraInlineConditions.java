package it.jira.condition;

import java.util.Map;
import java.util.Optional;

import com.atlassian.connect.test.jira.pageobjects.ViewIssuePageWithAddonFragments;
import com.atlassian.elasticsearch.shaded.google.common.base.Joiner;
import com.atlassian.plugin.connect.modules.beans.AddonUrlContext;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TestJiraInlineConditions extends AbstractJiraConditionsTest
{
    private static ConnectRunner runner;

    private static final String CONTEXT_PARAMETERIZED_WEBITEM = "context-parameterized";

    private static final ParameterCapturingConditionServlet PARAMETER_CAPTURING_SERVLET = new ParameterCapturingConditionServlet();

    private TestUser user;
    private String issueKey;

    @BeforeClass
    public static void startConnectAddon() throws Exception
    {
        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddonKey())
                .setAuthenticationToNone()
                .addModules("webItems", webItems())
                .addRoute(PARAMETER_CAPTURE_URL, PARAMETER_CAPTURING_SERVLET)
                .start();
    }

    private static WebItemModuleBean[] webItems()
    {
        return CONDITION_NAMES.stream().map(name -> newWebItemBean()
                .withName(new I18nProperty("Context Parameterized", CONTEXT_PARAMETERIZED_WEBITEM))
                .withKey(webItemKey(name))
                .withContext(AddonUrlContext.addon)
                .withLocation("operations-operations") // issue operations
                .withWeight(1)
                .withUrl(PARAMETER_CAPTURE_URL + "?condition={" + conditionVariable(name) + "}")
                .build()).collect(toList()).toArray(new WebItemModuleBean[CONDITION_NAMES.size()]);
    }

    private static String webItemKey(final String name)
    {
        return "test-web-item-" + name.replace("_", "-");
    }

    private static String conditionVariable(String conditionName)
    {
        Map<String, String> parameters = CONDITION_PARAMETERS.get(conditionName);
        String params = parameters == null ? "" :
                "(" + Joiner.on(",").withKeyValueSeparator("=").join(parameters.entrySet()) + ")";

        return String.format("condition.%s%s", conditionName, params);
    }

    @Before
    public void setUp()
    {
        user = testUserFactory.admin();
        issueKey = createIssueSatisfyingAllConditions(user);
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception
    {
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
    }

    @Test
    public void inlineConditionInWebItemsShouldEvaluateToTrue()
    {
        CONDITION_NAMES.forEach(name -> {
            clickWebItem(user, name);
            Map<String, String> conditionParams = PARAMETER_CAPTURING_SERVLET.getParamsFromLastRequest();
            assertThat(conditionParams, hasEntry(equalTo("condition"), equalTo("true")));
        });
    }

    private void clickWebItem(TestUser user, String conditionName)
    {
        ViewIssuePageWithAddonFragments viewIssuePage = loginAndVisit(user, ViewIssuePageWithAddonFragments.class, issueKey);
        String moduleKey = addonAndModuleKey(runner.getAddon().getKey(), webItemKey(conditionName));
        RemoteWebItem webItem = viewIssuePage.findWebItem(moduleKey, Optional.<String>empty());
        assertNotNull("Web item should be found", webItem);

        webItem.click();
    }
}
