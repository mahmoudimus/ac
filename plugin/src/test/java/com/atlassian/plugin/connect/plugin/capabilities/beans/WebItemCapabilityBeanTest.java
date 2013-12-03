package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.WebItemCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.atlassian.plugin.connect.plugin.capabilities.gson.CapabilitiesGsonFactory;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.capabilities.TestFileReader.readCapabilitiesTestFile;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.matchers.SameDeepPropertyValuesAs.sameDeepPropertyValuesAs;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean.newVendorBean;
import static org.junit.Assert.assertThat;


public class WebItemCapabilityBeanTest
{
    @Test
    public void producesCorrectBean() throws Exception
    {
        Gson gson = CapabilitiesGsonFactory.getGson();

        WebItemCapabilityBean webItemBean = createWebItemBeanBuilder().build();
        ConnectAddonBean addon = createAddonBeanBuilder(webItemBean).build();

        String json = readTestFile("defaultWebItemTest.json");
        ConnectAddonBean deserializedBean = gson.fromJson(json, ConnectAddonBean.class);

        assertThat(deserializedBean, sameDeepPropertyValuesAs(addon));
    }

    @Test
    public void producesBeanWithAbsoluteContext() throws Exception
    {
        Gson gson = CapabilitiesGsonFactory.getGson();

        WebItemCapabilityBean webItemBean = createWebItemBeanBuilder()
                .withContext(AddOnUrlContext.product)
                .build();
        ConnectAddonBean addon = createAddonBeanBuilder(webItemBean).build();

        String json = readTestFile("productContextWebItemTest.json");
        ConnectAddonBean deserializedBean = gson.fromJson(json, ConnectAddonBean.class);

        assertThat(deserializedBean, sameDeepPropertyValuesAs(addon));
    }

    @Test
    public void producesBeanWithDialogTarget() throws Exception
    {
        Gson gson = CapabilitiesGsonFactory.getGson();

        WebItemCapabilityBean webItemBuilder = createWebItemBeanBuilder()
                .withTarget(WebItemTarget.dialog)
                .build();
        ConnectAddonBean addon = createAddonBeanBuilder(webItemBuilder)
                .build();

        String json = readTestFile("dialogWebItemTest.json");
        ConnectAddonBean deserializedBean = gson.fromJson(json, ConnectAddonBean.class);

        assertThat(deserializedBean, sameDeepPropertyValuesAs(addon));
    }

    @Test
    public void producesBeanWithInlineDialogTarget() throws Exception
    {
        Gson gson = CapabilitiesGsonFactory.getGson();

        WebItemCapabilityBean webItemBean = createWebItemBeanBuilder()
                .withTarget(WebItemTarget.inlineDialog)
                .build();
        ConnectAddonBean addon = createAddonBeanBuilder(webItemBean).build();

        String json = readTestFile("inlineDialogWebItemTest.json");
        ConnectAddonBean deserializedBean = gson.fromJson(json, ConnectAddonBean.class);

        assertThat(deserializedBean, sameDeepPropertyValuesAs(addon));
    }

    private ConnectAddonBeanBuilder createAddonBeanBuilder(WebItemCapabilityBean webItemBean)
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
                .withCapability("webItems", webItemBean)
                .withAuthentication(
                        newAuthenticationBean()
                                .withType(AuthenticationType.OAUTH)
                                .withSharedKey("S0m3Publ1cK3y").build());
    }

    private WebItemCapabilityBeanBuilder createWebItemBeanBuilder()
    {
        return newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webItem"))
                .withLink("/my-general-page")
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
        return readCapabilitiesTestFile("webitem/" + filename);
    }
}
