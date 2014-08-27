package com.atlassian.plugin.connect.test.plugin.capabilities.beans;

import java.io.IOException;
import java.util.Map;

import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ModuleList;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.modules.beans.WebItemTargetType;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.WebItemModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;
import com.atlassian.plugin.connect.modules.gson.ProductlessConnectModulesGsonFactory;
import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemTargetBean.newWebItemTargetBean;
import static com.atlassian.plugin.connect.modules.beans.nested.VendorBean.newVendorBean;
import static com.atlassian.plugin.connect.test.plugin.capabilities.TestFileReader.readAddonTestFile;
import static com.atlassian.plugin.connect.test.plugin.capabilities.beans.matchers.SameDeepPropertyValuesAs.sameDeepPropertyValuesAs;
import static org.junit.Assert.assertThat;

public class WebItemModuleBeanTest
{
    @Test
    public void producesCorrectBean() throws Exception
    {
        WebItemModuleBean webItemBean = createWebItemBeanBuilder().build();
        ConnectAddonBean addon = createAddonBeanBuilder(webItemBean).build();

        String json = readTestFile("defaultWebItemTest.json");
        ConnectAddonBean<ModuleList> deserializedBean = ProductlessConnectModulesGsonFactory.addonFromJsonWithI18nCollector(json, null);

        assertThat(deserializedBean, sameDeepPropertyValuesAs(addon));
    }

    @Test
    public void producesBeanWithAbsoluteContext() throws Exception
    {
        WebItemModuleBean webItemBean = createWebItemBeanBuilder()
                .withContext(AddOnUrlContext.product)
                .build();
        ConnectAddonBean addon = createAddonBeanBuilder(webItemBean).build();

        String json = readTestFile("productContextWebItemTest.json");
        ConnectAddonBean<ModuleList> deserializedBean = ProductlessConnectModulesGsonFactory.addonFromJsonWithI18nCollector(json, null);

        assertThat(deserializedBean, sameDeepPropertyValuesAs(addon));
    }

    @Test
    public void producesBeanWithDialogTarget() throws Exception
    {
        WebItemTargetBean target = newWebItemTargetBean()
                .withType(WebItemTargetType.dialog)
                .build();
        WebItemModuleBean webItemBuilder = createWebItemBeanBuilder()
                .withTarget(target)
                .build();
        ConnectAddonBean addon = createAddonBeanBuilder(webItemBuilder)
                .build();

        String json = readTestFile("dialogWebItemTest.json");
        ConnectAddonBean<ModuleList> deserializedBean = ProductlessConnectModulesGsonFactory.addonFromJsonWithI18nCollector(json, null);

        assertThat(deserializedBean, sameDeepPropertyValuesAs(addon));
    }

    @Test
    public void producesBeanWithInlineDialogTarget() throws Exception
    {
        WebItemTargetBean target = newWebItemTargetBean()
                        .withType(WebItemTargetType.inlineDialog)
                        .build();
        WebItemModuleBean webItemBean = createWebItemBeanBuilder()
                .withTarget(target)
                .build();
        ConnectAddonBean addon = createAddonBeanBuilder(webItemBean).build();

        String json = readTestFile("inlineDialogWebItemTest.json");
        ConnectAddonBean<ModuleList> deserializedBean = ProductlessConnectModulesGsonFactory.addonFromJsonWithI18nCollector(json, null);

        assertThat(deserializedBean, sameDeepPropertyValuesAs(addon));
    }

    private ConnectAddonBeanBuilder createAddonBeanBuilder(WebItemModuleBean webItemBean)
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
                        .withWebItems(webItemBean)
                        .build())
                .withAuthentication(
                        newAuthenticationBean()
                                .withType(AuthenticationType.OAUTH)
                                .withPublicKey("S0m3Publ1cK3y").build());
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
