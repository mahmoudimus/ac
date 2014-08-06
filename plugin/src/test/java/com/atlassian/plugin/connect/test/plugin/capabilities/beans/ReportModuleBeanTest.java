package com.atlassian.plugin.connect.test.plugin.capabilities.beans;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ReportModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.atlassian.plugin.connect.test.plugin.capabilities.TestFileReader.readAddonTestFile;
import static org.junit.Assert.assertThat;

public class ReportModuleBeanTest
{

    @Test
    public void producesCorrectJSON() throws IOException
    {
        ReportModuleBean expectedBean = createBean();
        List<ReportModuleBean> addonBeans = readTestFile().getModules().getJiraReports();

        assertThat(addonBeans, Matchers.hasSize(1));
        assertThat(addonBeans, Matchers.hasItem(expectedBean));
    }

    private static ReportModuleBean createBean()
    {
        return ReportModuleBean.newBuilder()
                .withWeight(100)
                .withUrl("/report?projectId=${project.id}")
                .withDescription(new I18nProperty("description", "description i18n"))
                .withName(new I18nProperty("report", "report i18n"))
                .build();
    }

    private static ConnectAddonBean readTestFile() throws IOException
    {
        return ConnectModulesGsonFactory.getGson().fromJson(readAddonTestFile("reportAddon.json"), ConnectAddonBean.class);
    }
}
