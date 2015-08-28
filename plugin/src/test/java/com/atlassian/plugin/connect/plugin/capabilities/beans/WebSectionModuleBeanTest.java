package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.WebSectionModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.modules.gson.ModuleListDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean.newWebSectionBean;
import static com.atlassian.plugin.connect.testsupport.util.matcher.SameDeepPropertyValuesAs.sameDeepPropertyValuesAs;
import static com.atlassian.plugin.connect.util.io.TestFileReader.readAddonTestFile;
import static org.junit.Assert.assertThat;

public class WebSectionModuleBeanTest
{
    @Test
    public void producesCorrectBean() throws Exception
    {
        Gson gson = ConnectModulesGsonFactory.getGson();
        WebSectionModuleBean webSectionBean = createWebSectionBeanBuilder().build();
        String json = readTestFile("defaultWebSectionTest.json");
        WebSectionModuleBean deserializedBean = gson.fromJson(json, WebSectionModuleBean.class);
        assertThat(deserializedBean, sameDeepPropertyValuesAs(webSectionBean));
    }

    @Test
    public void producesCorrectBeanWithFunkyWebSections() throws Exception
    {
        final GsonBuilder gsonBuilder = ConnectModulesGsonFactory.getGsonBuilder();
        gsonBuilder.registerTypeAdapter(List.class, new ModuleListDeserializer<>(WebSectionModuleBean.class));
        Gson gson = gsonBuilder.create();

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
        
        List<WebSectionModuleBean> beans = new ArrayList<>(Arrays.asList(webSectionBean, hiddenSpoonSection, falafelSection));

        String json = readTestFile("funkyWebSectionTest.json");
        List<WebSectionModuleBean> deserializedBeans = gson.fromJson(json, List.class);
        assertThat(deserializedBeans, sameDeepPropertyValuesAs(beans));
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
