package com.atlassian.plugin.connect.jira.capabilities.beans;

import com.atlassian.plugin.connect.jira.capabilities.provider.ReportModuleProvider;
import com.atlassian.plugin.connect.modules.beans.*;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.nested.VendorBean.newVendorBean;

import static com.atlassian.plugin.connect.util.io.TestFileReader.readAddonTestFile;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class ReportModuleBeanTest
{

    @Test
    public void producesCorrectJSON() throws IOException
    {
        List<ReportModuleBean> beans = createBeans();
        
        Gson gson = ConnectModulesGsonFactory.getGson();
        String json = gson.toJson(beans, List.class);
        String expectedJson = readTestFile();

        assertThat(json, is(sameJSONAs(expectedJson)));
    }

    private static List<ReportModuleBean> createBeans()
    {
        return Arrays.asList(
            ReportModuleBean.newBuilder()
                .withKey("jira-report")
                .withWeight(5)
                .withUrl("/report?projectId=${project.id}")
                .withDescription(new I18nProperty("description", "description i18n"))
                .withName(new I18nProperty("report", "report i18n"))
                .withReportCategory(ReportCategory.AGILE)
                .withThumbnailUrl("/report-thumbnail.jpg")
                .build(),
            ReportModuleBean.newBuilder()
                .withKey("jira-report-2")
                .withUrl("/report?projectId=${project.id}")
                .withDescription(new I18nProperty("description 2", "description i18n"))
                .withName(new I18nProperty("report-2", "report i18n"))
                .build()
        );
    }

    private static String readTestFile() throws IOException
    {
        return readAddonTestFile("reportAddon.json");
    }
}
