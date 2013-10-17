package com.atlassian.plugin.connect.plugin.capabilities;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.gson.CapabilitiesGsonFactory;
import com.google.gson.Gson;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean.newIssueTabPageBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.RemoteContainerCapabilityBean.newRemoteContainerBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.OAuthBean.newOAuthBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean.newVendorBean;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @since version
 */
public class ConnectIssueTabPanelCapabilityBeanTest
{
    @Test
    public void producesCorrectJSON() throws Exception
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
                .withCapability(newIssueTabPageBean()
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

        assertThat(json, is(equalTo("{\"key\":\"my-plugin\",\"name\":\"My Plugin\",\"version\":\"1.0\",\"description\":" +
                "\"\",\"vendor\":{\"name\":\"Atlassian\",\"url\":\"http://www.atlassian.com\"},\"links\":{" +
                "\"self\":\"http://www.example.com/capabilities\",\"homepage\":\"http://www.example.com\"}," +
                "\"capabilities\":{\"connect-container\":{\"displayUrl\":\"http://www.example.com\",\"oauth\":{" +
                "\"publicKey\":\"S0m3Publ1cK3y\",\"callback\":\"\",\"requestTokenUrl\":\"\",\"accessTokenUrl\":\"\"," +
                "\"authorizeUrl\":\"\"}},\"issueTabPanels\":{\"url\":\"/my-general-page\",\"weight\":100,\"name\":" +
                "{\"value\":\"My Issue Tab Page\",\"i18n\":\"my.issueTabPage\"}}}}")));
    }
}
