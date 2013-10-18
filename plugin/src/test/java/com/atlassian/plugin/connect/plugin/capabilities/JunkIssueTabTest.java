package com.atlassian.plugin.connect.plugin.capabilities;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.gson.CapabilitiesGsonFactory;
import com.google.gson.Gson;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean.newIssueTabPanelBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.RemoteContainerCapabilityBean.newRemoteContainerBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.OAuthBean.newOAuthBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean.newVendorBean;

/**
 * @since version
 */
public class JunkIssueTabTest
{
    @Test
    public void testName() throws Exception
    {
        ConnectIssueTabPanelCapabilityBean bean = newIssueTabPanelBean()
                .withName(new I18nProperty("My Issue Tab Page", "my.issueTabPage"))
                .withUrl("/my-general-page")
                .withWeight(100)
                .build();

        Gson gson = CapabilitiesGsonFactory.getGson();

        String json = gson.toJson(bean,ConnectIssueTabPanelCapabilityBean.class);

        System.out.println(json);
    }

    @Test
    public void testAddon() throws Exception
    {
        Map<String,String> links = new HashMap<String,String>();
        links.put("self","http://www.example.com/capabilities");
        links.put("homepage","http://www.example.com");
        
        ConnectAddonBean addon = newConnectAddonBean()
                .withName("My Plugin")
                .withKey("my-plugin")
                .withVersion("1.0")
                .withLinks(links)
                .withVendor(newVendorBean().withName("Atlassian").withUrl("http://www.atlassian.com").build())
                .withCapability(newIssueTabPanelBean()
                        .withName(new I18nProperty("My Issue Tab Page", "my.issueTabPage"))
                        .withUrl("/my-general-page")
                        .withWeight(100)
                        .build())
                .withCapability(newRemoteContainerBean().withDisplayUrl("http://www.example.com").withOAuth(
                        newOAuthBean().withPublicKey("S0m3Publ1cK3y").build()
                ).build())
                .build();

        Gson gson = CapabilitiesGsonFactory.getGson();

        String json = gson.toJson(addon,ConnectAddonBean.class);

        System.out.println(json);
    }
}
