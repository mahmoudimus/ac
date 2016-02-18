package com.atlassian.plugin.connect.jira.permission;

import com.atlassian.plugin.connect.modules.beans.ProjectPermissionCategory;
import com.atlassian.plugin.connect.modules.beans.ProjectPermissionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.atlassian.plugin.connect.util.io.TestFileReader.readAddonTestFile;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class ProjectPermissionModuleBeanTest
{
    @Test
    public void producesCorrectJSON() throws IOException
    {
        List<ProjectPermissionModuleBean> projectPermissionModuleBean = Arrays.asList(readTestFile());
        System.out.println(projectPermissionModuleBean);
        assertThat(projectPermissionModuleBean, contains(
                ProjectPermissionModuleBean.newProjectPermissionModuleBean()
                        .withKey("jira-project-permission-1")
                        .withName(new I18nProperty("Name1", "name1.i18n"))
                        .withDescription(new I18nProperty("Description1", "description1.i18n"))
                        .withConditions(SingleConditionBean.newSingleConditionBean()
                                .withCondition("user_is_logged_in")
                                .build())
                        .build(),
                ProjectPermissionModuleBean.newProjectPermissionModuleBean()
                        .withKey("jira-project-permission-2")
                        .withName(new I18nProperty("Name2", "name2.i18n"))
                        .withDescription(new I18nProperty("Description2", "description2.i18n"))
                        .build()
        ));
    }

    @Test
    public void connectModuleProjectPermissionCategoriesShouldCorrespondToCategoriesInJira()
    {
        List<String> jiraCategories = Arrays.asList(com.atlassian.jira.permission.ProjectPermissionCategory.values())
                .stream().map(Enum::toString).collect(Collectors.toList());
        List<String> connectCategories = Arrays.asList(ProjectPermissionCategory.values())
                .stream().map(Enum::toString).collect(Collectors.toList());

        assertThat(jiraCategories, contains(connectCategories.toArray()));
    }

    private static ProjectPermissionModuleBean[] readTestFile() throws IOException
    {
        return ConnectModulesGsonFactory.getGson().fromJson(readAddonTestFile("projectPermissions.json"), ProjectPermissionModuleBean[].class);
    }
}