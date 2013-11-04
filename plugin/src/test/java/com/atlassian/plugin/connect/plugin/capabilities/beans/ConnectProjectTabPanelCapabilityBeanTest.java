package com.atlassian.plugin.connect.plugin.capabilities.beans;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.gson.CapabilitiesGsonFactory;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectTabPanelModuleProvider;

import com.google.gson.Gson;

import org.junit.Test;

import static com.atlassian.plugin.connect.plugin.capabilities.TestFileReader.readCapabilitiesTestFile;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectTabPanelCapabilityBean.newTabPanelBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.RemoteContainerCapabilityBean.newRemoteContainerBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.OAuthBean.newOAuthBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean.newVendorBean;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;


public class ConnectProjectTabPanelCapabilityBeanTest
{
    @Test
    public void producesCorrectJSON() throws Exception
    {
        Map<String, String> links = new HashMap<String, String>();
        links.put("self", "http://www.example.com/capabilities");
        links.put("homepage", "http://www.example.com");

        ConnectAddonBean addon = newConnectAddonBean()
                .withName("My Plugin")
                .withKey("my-plugin")
                .withVersion("1.0")
                .withLinks(links)
                .withVendor(newVendorBean().withName("Atlassian").withUrl("http://www.atlassian.com").build())
                .withCapability(ConnectTabPanelModuleProvider.PROJECT_TAB_PANELS, newTabPanelBean()
                        .withName(new I18nProperty("My Project Tab Page", "my.projectTabPage"))
                        .withUrl("/my-general-page")
                        .withWeight(100)
                        .build())
                .withCapability(RemoteContainerCapabilityBean.CONNECT_CONTAINER, newRemoteContainerBean().withDisplayUrl("http://www.example.com").withOAuth(
                        newOAuthBean().withPublicKey("S0m3Publ1cK3y").build()
                ).build())
                .build();

        Gson gson = CapabilitiesGsonFactory.getGson();

        String json = gson.toJson(addon, ConnectAddonBean.class);
        String expectedJson = readTestFile();

        assertThat(json, is(sameJSONAs(expectedJson)));
    }

    private static String readTestFile() throws IOException
    {
        return readCapabilitiesTestFile("projectTabAddon.json");
    }
}
