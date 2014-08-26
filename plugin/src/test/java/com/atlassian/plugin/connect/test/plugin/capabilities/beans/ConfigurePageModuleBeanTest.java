package com.atlassian.plugin.connect.test.plugin.capabilities.beans;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.JiraConfluenceModuleList;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.test.plugin.capabilities.beans.matchers.SameDeepPropertyValuesAs;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.test.plugin.capabilities.beans.matchers.SameDeepPropertyValuesAs.sameDeepPropertyValuesAs;
import static com.atlassian.plugin.connect.modules.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.modules.beans.nested.VendorBean.newVendorBean;
import static com.atlassian.plugin.connect.test.plugin.capabilities.TestFileReader.readAddonTestFile;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class ConfigurePageModuleBeanTest
{

    @Test
    public void producesCorrectJSON() throws Exception
    {
        ConnectAddonBean bean = createBean();
        Gson gson = ConnectModulesGsonFactory.getGson();
        String json = gson.toJson(bean, ConnectAddonBean.class);
        String expectedJson = readTestFile();

        assertThat(json, is(sameJSONAs(expectedJson)));
    }

    @Test
    public void producesCorrectBean() throws Exception
    {
        String json = readTestFile();
        Gson gson = ConnectModulesGsonFactory.getGson();
        ConnectAddonBean<JiraConfluenceModuleList> deserializedBean = ConnectModulesGsonFactory.addonFromJsonWithI18nCollector(json, null);
        ConnectAddonBean bean = createBean();

        assertThat(deserializedBean, SameDeepPropertyValuesAs.sameDeepPropertyValuesAs(bean));
    }

    @Test
    public void roundTrippingIsPreserving()
    {
        ConnectAddonBean originalBean = createBean();
        Gson gson = ConnectModulesGsonFactory.getGson();
        String json = gson.toJson(originalBean, ConnectAddonBean.class);
        ConnectAddonBean<JiraConfluenceModuleList> deserializedBean = ConnectModulesGsonFactory.addonFromJsonWithI18nCollector(json, null);

        assertThat(deserializedBean, SameDeepPropertyValuesAs.sameDeepPropertyValuesAs(originalBean));
    }

    private static ConnectAddonBean createBean()
    {
        return newConnectAddonBean()
                .withName("My Add-On")
                .withKey("my-add-on")
                .withVersion("2.0")
                .withBaseurl("http://www.example.com")
                .withVendor(newVendorBean().withName("Atlassian").withUrl("http://www.atlassian.com").build())
                .withModule("configurePage",
                        newPageBean()
                                .withName(new I18nProperty("Some page", "some.page.name"))
                                .withKey("configure-page")
                                .withLocation("")
                                .withUrl("/my-page")
                                .withIcon(newIconBean().withUrl("/mypage/icon.png").withHeight(80).withWidth(80).build())
                                .build()
                )
                .build();
    }

    private static String readTestFile() throws IOException
    {
        return readAddonTestFile("configurePageAddon.json");
    }

}
