package it.confluence.item;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.pageobjects.component.menu.AUIDropdownMenu;
import com.atlassian.confluence.pageobjects.component.menu.ConfluenceMenuItem;
import com.atlassian.confluence.pageobjects.component.menu.ToolsMenu;
import com.atlassian.confluence.pageobjects.page.content.ViewPage;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.plugin.connect.confluence.web.ConfluenceConditionClassResolver;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.SingleConditionBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;

import it.confluence.ConfluenceWebDriverTestBase;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.randomName;
import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Tests class resolution, autowiring and evaluation of some conditions in {@link ConfluenceConditionClassResolver}.
 */
public class TestConfluenceConditions extends ConfluenceWebDriverTestBase
{

    private static final List<String> CONDITION_NAMES = newArrayList(
            "can_edit_space_styles",
            "create_content",
            "email_address_public",
            "has_page",
            "has_space",
            "latest_version",
            "not_personal_space",
            "showing_page_attachments",
            "space_function_permission",
            "space_sidebar",
            "threaded_comments",
            "tiny_url_supported",
            "user_can_create_personal_space",
            "user_can_update_user_status",
            "user_logged_in_editable",
            "viewing_content"
    );

    private static final Map<String, Map<String, String>> CONDITION_PARAMETERS = ImmutableMap.of(
            "create_content", ImmutableMap.of("content", "Page"),
            "space_function_permission", ImmutableMap.of("permission", SpacePermission.VIEWSPACE_PERMISSION)
    );

    private static ConnectRunner addon;

    @BeforeClass
    public static void startAddon() throws Exception
    {
        addon = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .setAuthenticationToNone();
        addWebItemsWithConditions();
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

    @Test
    public void shouldDisplayWebItemsWithEachCondition()
    {
        Content page = createPage(randomName(TestConfluenceConditions.class.getSimpleName()), TestConfluenceConditions.class.getName());
        String pageId = String.valueOf(page.getId().asLong());

        ViewPage viewPage = loginAndVisit(testUserFactory.admin(), ViewPage.class, pageId);
        ToolsMenu toolsMenu = viewPage.openToolsMenu();

        List<String> passedConditions = CONDITION_NAMES.stream()
                .filter((conditionName) -> isItemPresentInMenu(toolsMenu, getDisplayNameForCondition(conditionName)))
                .collect(Collectors.toList());

        assertThat(passedConditions, equalTo(CONDITION_NAMES));
    }

    private static void addWebItemsWithConditions()
    {
        for (String conditionName : CONDITION_NAMES)
        {
            addon.addModules("webItems", newWebItemBeanWithCondition(conditionName));
        }
    }

    private static WebItemModuleBean newWebItemBeanWithCondition(String conditionName)
    {
        SingleConditionBeanBuilder conditionBeanBuilder = newSingleConditionBean().withCondition(conditionName);
        if (CONDITION_PARAMETERS.containsKey(conditionName))
        {
            conditionBeanBuilder.withParams(CONDITION_PARAMETERS.get(conditionName));
        }
        return newWebItemBean()
                .withKey(conditionName.replace('_', '-'))
                .withUrl("/path-without-route")
                .withLocation("system.content.action/primary")
                .withWeight(CONDITION_NAMES.indexOf(conditionName))
                .withName(new I18nProperty(getDisplayNameForCondition(conditionName), null))
                .withConditions(conditionBeanBuilder.build())
                .build();
    }

    private static String getDisplayNameForCondition(String conditionName)
    {
        return String.format("%d %s", CONDITION_NAMES.indexOf(conditionName), StringUtils.substring(conditionName, 0, 15));
    }

    private boolean isItemPresentInMenu(AUIDropdownMenu menu, String displayNameForCondition)
    {
        ConfluenceMenuItem menuItem = menu.getMenuItem(By.linkText(displayNameForCondition));
        return menuItem.isPresent();
    }
}
