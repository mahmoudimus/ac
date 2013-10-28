package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.opensymphony.util.FileUtils;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectProjectAdminTabPanelCapabilityBean.newProjectAdminTabPanelBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.RemoteContainerCapabilityBean.newRemoteContainerBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.OAuthBean.newOAuthBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean.newVendorBean;
import static com.atlassian.plugin.connect.plugin.capabilities.gson.CapabilitiesGsonFactory.getGson;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;


public class ConnectProjectAdminTabPanelCapabilityBeanTest
{
    @Test
    public void producesCorrectJSON() throws Exception
    {
        String json = getGson().toJson(createBean(), ConnectAddonBean.class);
        assertThat(json, is(sameJSONAs(expectedJson())));
    }

    @Test
    public void producesExactlyOneProjectAdminTabPanelCapability()
    {
        List<? extends CapabilityBean> capabilityBeans = createBean().getCapabilities().get("projectAdminTabPanels");

        assertThat(capabilityBeans, hasSize(1));
    }

    @Test
    public void producesCapabilityIsCorrectType()
    {
        CapabilityBean capabilityBean = createBean().getCapabilities().get("projectAdminTabPanels").get(0);

        assertThat(capabilityBean, is(instanceOf(ConnectProjectAdminTabPanelCapabilityBean.class)));
    }

    @Test
    public void prefixesLocationCorrectly()
    {
        ConnectProjectAdminTabPanelCapabilityBean capabilityBean =
                (ConnectProjectAdminTabPanelCapabilityBean) createBean().getCapabilities().get("projectAdminTabPanels").get(0);

        assertThat(capabilityBean.getAbsoluteLocation(), is("atl.jira.proj.config/a-location"));
    }

    private ConnectAddonBean createBean()
    {
        Map<String, String> links = new HashMap<String, String>();
        links.put("self", "http://www.example.com/capabilities");
        links.put("homepage", "http://www.example.com");

        return newConnectAddonBean()
                .withName("My Plugin")
                .withKey("my-plugin")
                .withVersion("1.0")
                .withLinks(links)
                .withVendor(newVendorBean().withName("Atlassian").withUrl("http://www.atlassian.com").build())
                .withCapability(newProjectAdminTabPanelBean()
                        .withName(new I18nProperty("My ProjectAdmin Tab Page", "my.projectAdminTabPage"))
                        .withUrl("/my-general-page")
                        .withWeight(100)
                        .withLocation("a-location")
                        .build())
                .withCapability(newRemoteContainerBean().withDisplayUrl("http://www.example.com").withOAuth(
                        newOAuthBean().withPublicKey("S0m3Publ1cK3y").build()
                ).build()
                ).build();
    }

    private static String expectedJson() throws IOException
    {
        return FileUtils.readFile(new DefaultResourceLoader().getResource("classpath:/testfiles/capabilities/projectAdminTabAddon.json").getFile());
    }
}
