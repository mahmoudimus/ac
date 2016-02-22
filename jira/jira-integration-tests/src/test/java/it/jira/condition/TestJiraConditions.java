package it.jira.condition;

import java.util.List;

import com.atlassian.jira.pageobjects.pages.viewissue.IssueMenu;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.plugin.connect.jira.web.condition.JiraConditionClassResolver;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.SingleConditionBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.connect.test.common.util.TestUser;
import it.jira.item.TestJiraWebItemWithProductCondition;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Tests class resolution, autowiring and evaluation of most conditions in {@link JiraConditionClassResolver}.
 *
 * Conditions covered elsewhere:
 * - is_admin_mode - {@link TestJiraWebItemWithProductCondition#shouldPerformActionForWebItemWithAdminModeCondition()}
 * - entity_property_equal_to - {@link TestEntityPropertyEqualToCondition}
 *
 * Not covered:
 * - user_is_the_logged_in_user
 * - user_has_issue_history
 * - is_issue_reported_by_current_user
 */
public class TestJiraConditions extends AbstractJiraConditionsTest {
    private static ConnectRunner addon;

    @BeforeClass
    public static void startAddon() throws Exception {
        addon = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddonKey())
                .setAuthenticationToNone();
        addWebItemsWithConditions();
        addon.start();
    }

    @AfterClass
    public static void stopAddon() throws Exception {
        if (addon != null) {
            addon.stopAndUninstall();
        }
    }

    @Test
    public void shouldDisplayWebItemsWithEachCondition() {
        TestUser admin = testUserFactory.admin();
        login(admin);

        String issueKey = createIssueSatisfyingAllConditions(admin);

        ViewIssuePage viewIssuePage = product.visit(ViewIssuePage.class, issueKey);
        IssueMenu issueMenu = viewIssuePage.getIssueMenu();
        issueMenu.openMoreActions();

        List<String> passedConditions = CONDITIONS.stream()
                .filter((nameParams) -> isItemPresentInMoreActionsMenu(issueMenu, getDisplayNameForCondition(nameParams)))
                .map(TestedCondition::getName)
                .collect(toList());

        assertThat(passedConditions, equalTo(CONDITIONS.stream().map(TestedCondition::getName).collect(toList())));
    }

    private static void addWebItemsWithConditions() {
        for (TestedCondition condition : CONDITIONS) {
            addon.addModules("webItems", newWebItemBeanWithCondition(condition));
        }
    }

    private static WebItemModuleBean newWebItemBeanWithCondition(TestedCondition condition) {
        SingleConditionBeanBuilder conditionBeanBuilder = newSingleConditionBean().withCondition(condition.getName());
        if (!condition.getParameters().isEmpty()) {
            conditionBeanBuilder.withParams(condition.getParameters());
        }
        return newWebItemBean()
                .withKey(condition.getName().replace('_', '-') + "-" + RandomStringUtils.randomNumeric(10))
                .withUrl("/path-without-route")
                .withLocation("operations-work")
                .withWeight(CONDITIONS.indexOf(condition))
                .withName(new I18nProperty(getDisplayNameForCondition(condition), null))
                .withConditions(conditionBeanBuilder.build())
                .build();
    }

    private static String getDisplayNameForCondition(TestedCondition condition) {
        return String.format("%d %s", CONDITIONS.indexOf(condition), StringUtils.substring(condition.getName(), 0, 15));
    }

    private boolean isItemPresentInMoreActionsMenu(IssueMenu issueMenu, String webItemTitle) {
        return issueMenu.isItemPresentInMoreActionsMenu(webItemTitle);
    }
}
