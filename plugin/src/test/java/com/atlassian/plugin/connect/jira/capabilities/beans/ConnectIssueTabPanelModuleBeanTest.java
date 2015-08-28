package com.atlassian.plugin.connect.jira.capabilities.beans;

import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.util.io.TestFileReader.readAddonTestFile;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;


public class ConnectIssueTabPanelModuleBeanTest
{
    @Test
    public void producesCorrectJSON() throws Exception
    {
        ConnectTabPanelModuleBean moduleBean = newTabPanelBean()
                .withName(new I18nProperty("My Issue Tab Page", "my.issueTabPage"))
                .withUrl("/my-general-page")
                .withWeight(100)
                .build();

        Gson gson = ConnectModulesGsonFactory.getGson();
        String json = gson.toJson(moduleBean, ConnectTabPanelModuleBean.class);
        String expectedJson = readTestFile();

        assertThat(json, is(sameJSONAs(expectedJson)));
    }

    

    private static String readTestFile() throws IOException
    {
        return readAddonTestFile("issueTabAddon.json");
    }
}
