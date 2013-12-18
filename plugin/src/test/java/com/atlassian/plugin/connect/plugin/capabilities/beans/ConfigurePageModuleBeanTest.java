package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.gson.ConnectModulesGsonFactory;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.plugin.capabilities.TestFileReader.readAddonTestFile;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConfigurePageModuleBean.newConfigurePageBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.SameDeepPropertyValuesAs.sameDeepPropertyValuesAs;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean.newVendorBean;
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
        System.out.println(json);
        String expectedJson = readTestFile();

        assertThat(json, is(sameJSONAs(expectedJson)));
    }

    @Test
    public void producesCorrectBean() throws Exception
    {
        String json = readTestFile();
        Gson gson = ConnectModulesGsonFactory.getGson();
        ConnectAddonBean deserializedBean = gson.fromJson(json, ConnectAddonBean.class);
        ConnectAddonBean bean = createBean();

        System.out.println(bean.getModules().getConfigurePages());
        System.out.println(deserializedBean.getModules().getConfigurePages());

        assertThat(deserializedBean, sameDeepPropertyValuesAs(bean));
    }

    @Test
    public void roundTrippingIsPreserving()
    {
        ConnectAddonBean originalBean = createBean();
        Gson gson = ConnectModulesGsonFactory.getGson();
        String json = gson.toJson(originalBean, ConnectAddonBean.class);
        ConnectAddonBean deserializedBean = gson.fromJson(json, ConnectAddonBean.class);

        assertThat(deserializedBean, sameDeepPropertyValuesAs(originalBean));
    }

    private static ConnectAddonBean createBean()
    {
        return newConnectAddonBean()
                .withName("My Add-On")
                .withKey("my-add-on")
                .withVersion("2.0")
                .withBaseurl("http://www.example.com")
                .withVendor(newVendorBean().withName("Atlassian").withUrl("http://www.atlassian.com").build())
                .withModules("configurePages",
                        newConfigurePageBean()
                                .withName(new I18nProperty("Some page", "some.page.name"))
                                .withKey("")
                                .withLocation("")
                                .withUrl("/my-page")
                                .withIcon(newIconBean().withUrl("/mypage/icon.png").withHeight(80).withWidth(80).build())
                                .build(),
                        newConfigurePageBean()
                                .setAsDefault()
                                .withName(new I18nProperty("Some otro page", "some.page2.name"))
                                .withKey("")
                                .withLocation("")
                                .withUrl("/my-page2")
                                .withIcon(newIconBean().withUrl("/mypage2/icon.png").withHeight(80).withWidth(80).build())
                                .build()
                )
                .build();
    }

    private static String readTestFile() throws IOException
    {
        return readAddonTestFile("configurePageAddon.json");
    }

}
