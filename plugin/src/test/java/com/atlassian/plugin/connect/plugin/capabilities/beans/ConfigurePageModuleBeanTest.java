package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.testsupport.util.matcher.SameDeepPropertyValuesAs;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.util.io.TestFileReader.readAddonTestFile;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class ConfigurePageModuleBeanTest
{

    @Test
    public void producesCorrectJSON() throws Exception
    {
        ConnectPageModuleBean bean = createBean();
        Gson gson = ConnectModulesGsonFactory.getGson();
        String json = gson.toJson(bean, ConnectPageModuleBean.class);
        String expectedJson = readTestFile();

        assertThat(json, is(sameJSONAs(expectedJson)));
    }

    @Test
    public void producesCorrectBean() throws Exception
    {
        String json = readTestFile();
        Gson gson = ConnectModulesGsonFactory.getGson();
        ConnectPageModuleBean deserializedBean = gson.fromJson(json, ConnectPageModuleBean.class);
        ConnectPageModuleBean bean = createBean();

        assertThat(deserializedBean, SameDeepPropertyValuesAs.sameDeepPropertyValuesAs(bean));
    }

    @Test
    public void roundTrippingIsPreserving()
    {
        ConnectPageModuleBean originalBean = createBean();
        Gson gson = ConnectModulesGsonFactory.getGson();
        String json = gson.toJson(originalBean, ConnectPageModuleBean.class);
        ConnectPageModuleBean deserializedBean = gson.fromJson(json, ConnectPageModuleBean.class);

        assertThat(deserializedBean, SameDeepPropertyValuesAs.sameDeepPropertyValuesAs(originalBean));
    }

    private static ConnectPageModuleBean createBean()
    {
        return newPageBean()
            .withName(new I18nProperty("Some page", "some.page.name"))
            .withKey("configure-page")
            .withLocation("")
            .withUrl("/my-page")
            .withIcon(newIconBean().withUrl("/mypage/icon.png").withHeight(80).withWidth(80).build())
            .build();
    }

    private static String readTestFile() throws IOException
    {
        return readAddonTestFile("configurePageAddon.json");
    }

}
