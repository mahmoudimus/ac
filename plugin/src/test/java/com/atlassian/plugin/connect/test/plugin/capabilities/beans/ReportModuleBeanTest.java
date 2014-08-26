package com.atlassian.plugin.connect.test.plugin.capabilities.beans;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.JiraConfluenceModuleList;
import com.atlassian.plugin.connect.modules.beans.ReportModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.atlassian.plugin.connect.test.plugin.capabilities.TestFileReader.readAddonTestFile;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class ReportModuleBeanTest
{

    @Test
    public void producesCorrectJSON() throws IOException
    {
        List<ReportModuleBean> addonBeans = readTestFile().getModules().getJiraReports();

        assertThat(addonBeans, hasSize(2));
        assertThat(addonBeans, contains(createBeans()));
    }

    private static ReportModuleBean[] createBeans()
    {
        return new ReportModuleBean[] {
            ReportModuleBean.newBuilder()
                .withKey("jira-report")
                .withWeight(5)
                .withUrl("/report?projectId=${project.id}")
                .withDescription(new I18nProperty("description", "description i18n"))
                .withName(new I18nProperty("report", "report i18n"))
                .build(),
            ReportModuleBean.newBuilder()
                .withKey("jira-report-2")
                .withUrl("/report?projectId=${project.id}")
                .withDescription(new I18nProperty("description 2", "description i18n"))
                .withName(new I18nProperty("report-2", "report i18n"))
                .build()
        };
    }

    private static ConnectAddonBean<JiraConfluenceModuleList> readTestFile() throws IOException
    {
        return ConnectModulesGsonFactory.addonFromJsonWithI18nCollector(
                readAddonTestFile("reportAddon.json"), null);
    }
}
