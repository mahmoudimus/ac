package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.ConnectAddonBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.IconBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.WebItemCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.atlassian.plugin.connect.plugin.capabilities.gson.CapabilitiesGsonFactory;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.capabilities.TestFileReader.readCapabilitiesTestFile;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean.newVendorBean;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;


public class WebItemCapabilityBeanTest
{

    @Test
    public void producesCorrectJSON() throws Exception
    {
        Map<String, String> links = new HashMap<String, String>();
        links.put("self", "http://www.example.com/capabilities");
        links.put("homepage", "http://www.example.com");

        WebItemCapabilityBean webItemBean = createWebItemBeanBuilder().build();
        ConnectAddonBean addon = createAddonBeanBuilder(links, webItemBean).build();

        Gson gson = CapabilitiesGsonFactory.getGson();

        String json = gson.toJson(addon, ConnectAddonBean.class);
        String expectedJson = readTestFile("defaultWebItemTest.json");

        assertThat(json, is(sameJSONAs(expectedJson)));
    }

    @Test
    public void producesCorrectJSONWithDialogTarget() throws Exception
    {
        Map<String, String> links = new HashMap<String, String>();
        links.put("self", "http://www.example.com/capabilities");
        links.put("homepage", "http://www.example.com");

        WebItemCapabilityBean webItemBuilder = createWebItemBeanBuilder()
                .withTarget(WebItemTarget.dialog)
                .build();
        ConnectAddonBean addon = createAddonBeanBuilder(links, webItemBuilder)
                .build();

        Gson gson = CapabilitiesGsonFactory.getGson();

        String json = gson.toJson(addon, ConnectAddonBean.class);
        String expectedJson = readTestFile("dialogWebItemTest.json");

        assertThat(json, is(sameJSONAs(expectedJson)));
    }

    @Test
    public void producesCorrectJSONWithInlineDialogTarget() throws Exception
    {
        Map<String, String> links = new HashMap<String, String>();
        links.put("self", "http://www.example.com/capabilities");
        links.put("homepage", "http://www.example.com");

        WebItemCapabilityBean webItemBean = createWebItemBeanBuilder()
                .withTarget(WebItemTarget.inlineDialog)
                .build();
        ConnectAddonBean addon = createAddonBeanBuilder(links, webItemBean).build();

        Gson gson = CapabilitiesGsonFactory.getGson();

        String json = gson.toJson(addon, ConnectAddonBean.class);
        String expectedJson = readTestFile("inlineDialogWebItemTest.json");

        assertThat(json, is(sameJSONAs(expectedJson)));
    }

    private ConnectAddonBeanBuilder createAddonBeanBuilder(Map<String, String> links, WebItemCapabilityBean webItemBean)
    {
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
