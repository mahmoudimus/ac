package com.atlassian.plugin.connect.plugin.capabilities.gson;

import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;

import com.google.gson.Gson;

import org.junit.Test;

import static com.atlassian.plugin.connect.plugin.capabilities.TestFileReader.readCapabilitiesTestFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the basic marshalling of capability beans
 * <p/>
 * This is the only place where we should actually have to test the marshalling as the adapter factory handles everything.
 * This is also the only class that should be using hard-coded json strings.
 */
public class ConnectAddonBeanMarshallingTest
{
    /**
     * Just verifies the basic marshalling of the core properties for the top-level add on bean
     *
     * @throws Exception
     */
    @Test
    public void verifyAddonValues() throws Exception
    {
        String json = readCapabilitiesTestFile("addonNoCapabilities.json");

        Gson gson = CapabilitiesGsonFactory.getGson();
        ConnectAddonBean addOn = gson.fromJson(json, ConnectAddonBean.class);

        assertEquals("My Plugin", addOn.getName());
        assertEquals("a cool addon", addOn.getDescription());
        assertEquals("my-plugin", addOn.getKey());
        assertEquals("Atlassian", addOn.getVendor().getName());
        assertEquals("http://www.atlassian.com", addOn.getVendor().getUrl());
        assertEquals(2, addOn.getLinks().size());
        assertEquals("http://www.example.com", addOn.getLinks().get("homepage"));
        assertEquals("http://www.example.com/capabilities", addOn.getLinks().get("self"));
    }

    /**
     * Verifies that extra stuff in the json that an external developer may put in is ignored without exceptions
     *
     * @throws Exception
     */
    @Test
    public void verifyExtraValuesAreIgnored() throws Exception
    {
        String json = readCapabilitiesTestFile("addonExtraValue.json");

        Gson gson = CapabilitiesGsonFactory.getGson();
        ConnectAddonBean addOn = gson.fromJson(json, ConnectAddonBean.class);

        assertEquals("My Plugin", addOn.getName());
        assertEquals("a cool addon", addOn.getDescription());
        assertEquals("my-plugin", addOn.getKey());
        assertEquals("Atlassian", addOn.getVendor().getName());
        assertEquals("http://www.atlassian.com", addOn.getVendor().getUrl());
        assertEquals(2, addOn.getLinks().size());
        assertEquals("http://www.example.com", addOn.getLinks().get("homepage"));
        assertEquals("http://www.example.com/capabilities", addOn.getLinks().get("self"));
    }

    /**
     * Verifies that a descriptor without a capabilities entry returns an empty capabilities map
     *
     * @throws Exception
     */
    @Test
    public void noCapabilitiesReturnsEmptyList() throws Exception
    {
        String json = readCapabilitiesTestFile("addonNoCapabilities.json");

        Gson gson = CapabilitiesGsonFactory.getGson();
        ConnectAddonBean addOn = gson.fromJson(json, ConnectAddonBean.class);

        assertNotNull(addOn.getCapabilities());
    }

    /**
     * Tests that a capability whose value is an object gets transformed into a list of one
     *
     * @throws Exception
     */
    @Test
    public void singleCapability() throws Exception
    {
        String json = readCapabilitiesTestFile("addonSingleCapability.json");

        Gson gson = CapabilitiesGsonFactory.getGson();
        ConnectAddonBean addOn = gson.fromJson(json, ConnectAddonBean.class);

        List<WebItemCapabilityBean> moduleList = addOn.getCapabilities().getWebItems();

        assertEquals(1, moduleList.size());

        WebItemCapabilityBean module = moduleList.get(0);

        assertEquals("a web item", module.getName().getValue());
    }

    /**
     * Verifies that multiple capability entries with mixed object / array values is marshalled properly
     *
     * @throws Exception
     */
    @Test
    public void multiCapabilities() throws Exception
    {
        String json = readCapabilitiesTestFile("addonMultipleCapabilities.json");

        Gson gson = CapabilitiesGsonFactory.getGson();
        ConnectAddonBean addOn = gson.fromJson(json, ConnectAddonBean.class);

        List<WebItemCapabilityBean> moduleList = addOn.getCapabilities().getWebItems();
        RemoteContainerCapabilityBean containerBean = addOn.getCapabilities().getConnectContainer();

        assertEquals(2, moduleList.size());
        assertEquals("a web item", moduleList.get(0).getName().getValue());
        assertEquals("another web item", moduleList.get(1).getName().getValue());

        assertNotNull(containerBean);
        assertEquals("http://www.example.com", containerBean.getDisplayUrl());
    }
}
