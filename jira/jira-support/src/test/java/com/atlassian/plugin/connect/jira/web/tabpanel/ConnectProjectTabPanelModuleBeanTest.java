package com.atlassian.plugin.connect.jira.web.tabpanel;

import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean.newTabPanelBean;
import static com.atlassian.plugin.connect.test.TestFileReader.readAddonTestFile;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;


public class ConnectProjectTabPanelModuleBeanTest {
    @Test
    public void producesCorrectJSON() throws Exception {
        ConnectTabPanelModuleBean bean = newTabPanelBean()
                .withName(new I18nProperty("My Project Tab Page", "my.projectTabPage"))
                .withUrl("/my-general-page")
                .withWeight(100)
                .build();

        Gson gson = ConnectModulesGsonFactory.getGson();

        String json = gson.toJson(bean, ConnectTabPanelModuleBean.class);
        String expectedJson = readTestFile();

        assertThat(json, is(sameJSONAs(expectedJson)));
    }

    private static String readTestFile() throws IOException {
        return readAddonTestFile("projectTabAddon.json");
    }
}
