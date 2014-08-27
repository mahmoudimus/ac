package com.atlassian.plugin.connect.test.plugin.capabilities.beans;

import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.JiraConfluenceModuleList;
import com.atlassian.plugin.connect.modules.beans.ModuleList;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.WebSectionModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.gson.JiraConfluenceConnectModulesGsonFactory;
import com.atlassian.plugin.connect.modules.gson.ProductlessConnectModulesGsonFactory;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean.newWebSectionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.VendorBean.newVendorBean;
import static com.atlassian.plugin.connect.test.plugin.capabilities.TestFileReader.readAddonTestFile;
import static com.atlassian.plugin.connect.test.plugin.capabilities.beans.matchers.SameDeepPropertyValuesAs.sameDeepPropertyValuesAs;
import static org.junit.Assert.assertThat;

public class WebSectionModuleBeanTest
{
    @Test
    public void producesCorrectBean() throws Exception
    {
        WebSectionModuleBean webSectionBean = createWebSectionBeanBuilder().build();
        ConnectAddonBean addon = createAddonBeanBuilder(webSectionBean).build();

        String json = readTestFile("defaultWebSectionTest.json");
        ConnectAddonBean<ModuleList> deserializedBean = ProductlessConnectModulesGsonFactory.addonFromJsonWithI18nCollector(json, null);

        assertThat(deserializedBean, sameDeepPropertyValuesAs(addon));
    }

    @Test
    public void producesCorrectBeanWithFunkyWebSections() throws Exception
    {
        WebSectionModuleBean webSectionBean = createWebSectionBeanBuilder().build();

        WebSectionModuleBean hiddenSpoonSection = newWebSectionBean()
                        .withName(new I18nProperty("THERE IZ NO ∫Pººñ", null))
                        .withKey("∫πººñ")
                        .withLocation("hidden")
                        .withWeight(999)
                        .build();

        WebSectionModuleBean falafelSection = newWebSectionBean()
                        .withName(new I18nProperty("أنا أحب الفلافل", "i.love.falafel"))
                        .withKey("my-falafel-section")
                        .withLocation("recipes")
                        .withTooltip(new I18nProperty("Chickpeas and hummous", "falafel.ingredients"))
                        .build();

        ConnectAddonBean addon = createAddonBeanBuilder(webSectionBean, hiddenSpoonSection, falafelSection).build();

        String json = readTestFile("funkyWebSectionTest.json");
        ConnectAddonBean<ModuleList> deserializedBean = ProductlessConnectModulesGsonFactory.addonFromJsonWithI18nCollector(json, null);

        assertThat(deserializedBean, sameDeepPropertyValuesAs(addon));
    }

    private ConnectAddonBeanBuilder createAddonBeanBuilder(WebSectionModuleBean... webSectionBeans)
    {
        Map<String, String> links = MapBuilder.build(
                "self", "http://www.example.com/capabilities",
                "homepage", "http://www.example.com"
        );

        return newConnectAddonBean()
                .withName("My Plugin")
                .withKey("my-plugin")
                .withVersion("1.0")
                .withLinks(links)
                .withBaseurl("http://www.example.com")
                .withVendor(newVendorBean().withName("Atlassian").withUrl("http://www.atlassian.com").build())
                .withModuleList(ModuleList.newModuleList()
                        .withWebSections(webSectionBeans)
                        .build())
                .withAuthentication(
                        newAuthenticationBean()
                                .withType(AuthenticationType.OAUTH)
                                .withPublicKey("S0m3Publ1cK3y").build());
    }

    private WebSectionModuleBeanBuilder createWebSectionBeanBuilder()
    {
        return newWebSectionBean()
                .withName(new I18nProperty("My Web Section", "i.love.plugins"))
                .withKey("my-web-section")
                .withLocation("system.preset.filters")
                .withTooltip(new I18nProperty("Does batman even need robin?", "batman.robin"))
                .withWeight(200);
    }

    private static String readTestFile(String filename) throws IOException
    {
        return readAddonTestFile("websection/" + filename);
    }
}
