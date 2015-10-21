package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.builder.WebItemModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.testsupport.util.matcher.SameDeepPropertyValuesAs.sameDeepPropertyValuesAs;
import static com.atlassian.plugin.connect.util.io.TestFileReader.readAddonTestFile;
import static org.junit.Assert.assertThat;

public class WebItemModuleBeanTest
{
    @Test
    public void producesCorrectBean() throws Exception
    {
        Gson gson = ConnectModulesGsonFactory.getGson();

        WebItemModuleBean webItemBean = createWebItemBeanBuilder().build();

        String json = readTestFile("defaultWebItemTest.json");
        WebItemModuleBean deserializedBean = gson.fromJson(json, WebItemModuleBean.class);

        assertThat(deserializedBean, sameDeepPropertyValuesAs(webItemBean));
    }

    @Test
    public void producesBeanWithAbsoluteContext() throws Exception
    {
        Gson gson = ConnectModulesGsonFactory.getGson();

        WebItemModuleBean webItemBean = createWebItemBeanBuilder()
                .withContext(AddOnUrlContext.product)
                .build();

        String json = readTestFile("productContextWebItemTest.json");
        WebItemModuleBean deserializedBean = gson.fromJson(json, WebItemModuleBean.class);

        assertThat(deserializedBean, sameDeepPropertyValuesAs(webItemBean));
    }

    @Test
    public void producesBeanWithDialogTarget() throws Exception
    {
        Gson gson = ConnectModulesGsonFactory.getGson();

        WebItemTargetBean target = newWebItemTargetBean()
                .withType(WebItemTargetType.dialog)
                .build();
        WebItemModuleBean webItemBean = createWebItemBeanBuilder()
                .withTarget(target)
                .build();

        String json = readTestFile("dialogWebItemTest.json");
        WebItemModuleBean deserializedBean = gson.fromJson(json, WebItemModuleBean.class);

        assertThat(deserializedBean, sameDeepPropertyValuesAs(webItemBean));
    }

    @Test
    public void producesBeanWithInlineDialogTarget() throws Exception
    {
        Gson gson = ConnectModulesGsonFactory.getGson();

        WebItemTargetBean target = newWebItemTargetBean()
                        .withType(WebItemTargetType.inlineDialog)
                        .build();
        WebItemModuleBean webItemBean = createWebItemBeanBuilder()
                .withTarget(target)
                .build();

        String json = readTestFile("inlineDialogWebItemTest.json");
        WebItemModuleBean deserializedBean = gson.fromJson(json, WebItemModuleBean.class);

        assertThat(deserializedBean, sameDeepPropertyValuesAs(webItemBean));
    }

    private WebItemModuleBeanBuilder createWebItemBeanBuilder()
    {
        return newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webItem"))
                .withKey("my-web-item")
                .withUrl("/my-general-page")
                .withLocation("system.preset.filters")
                .withIcon(IconBean.newIconBean()
                        .withHeight(16)
                        .withWidth(20)
                        .withUrl("http://lolcats.com/lol.gif")
                        .build())
                .withStyleClasses("clowns", "are-not", "funny")
                .withTooltip(new I18nProperty("Does batman even need robin?", "batman.robin"))
                .withWeight(200);
    }

    private static String readTestFile(String filename) throws IOException
    {
        return readAddonTestFile("webitem/" + filename);
    }
}