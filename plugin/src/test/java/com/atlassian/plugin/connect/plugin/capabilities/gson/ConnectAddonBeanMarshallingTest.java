package com.atlassian.plugin.connect.plugin.capabilities.gson;

import com.atlassian.plugin.connect.api.scopes.ScopeName;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.google.gson.Gson;
import org.junit.Test;

import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.TestFileReader.readAddonTestFile;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the basic marshalling of module beans
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
        String json = readAddonTestFile("addonNoCapabilities.json");

        Gson gson = ConnectModulesGsonFactory.getGson();
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
        String json = readAddonTestFile("addonExtraValue.json");

        Gson gson = ConnectModulesGsonFactory.getGson();
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
        String json = readAddonTestFile("addonNoCapabilities.json");

        Gson gson = ConnectModulesGsonFactory.getGson();
        ConnectAddonBean addOn = gson.fromJson(json, ConnectAddonBean.class);

        assertNotNull(addOn.getModules());
    }

    /**
     * Tests that a module whose value is an object gets transformed into a list of one
     *
     * @throws Exception
     */
    @Test
    public void singleModule() throws Exception
    {
        String json = readAddonTestFile("addonSingleCapability.json");

        Gson gson = ConnectModulesGsonFactory.getGson();
        ConnectAddonBean addOn = gson.fromJson(json, ConnectAddonBean.class);

        List<WebItemModuleBean> moduleList = addOn.getModules().getWebItems();

        assertEquals(1, moduleList.size());

        WebItemModuleBean module = moduleList.get(0);

        assertEquals("a web item", module.getName().getValue());
    }

    /**
     * Verifies that multiple module entries with mixed object / array values is marshalled properly
     *
     * @throws Exception
     */
    @Test
    public void multiCapabilities() throws Exception
    {
        String json = readAddonTestFile("addonMultipleCapabilities.json");

        Gson gson = ConnectModulesGsonFactory.getGson();
        ConnectAddonBean addOn = gson.fromJson(json, ConnectAddonBean.class);

        List<WebItemModuleBean> moduleList = addOn.getModules().getWebItems();

        assertEquals(2, moduleList.size());
        assertEquals("a web item", moduleList.get(0).getName().getValue());
        assertEquals("another web item", moduleList.get(1).getName().getValue());

        assertEquals("http://www.example.com", addOn.getBaseUrl());
    }

    @Test
    public void noScopes() throws IOException
    {
        String json = readCapabilitiesTestFile("addonMultipleCapabilities.json");
        ConnectAddonBean addOn = CapabilitiesGsonFactory.getGson().fromJson(json, ConnectAddonBean.class);
        assertThat(addOn.getScopes(), is(Collections.<ScopeName>emptySet()));
    }

    @Test
    public void singleScope() throws IOException
    {
        String json = readCapabilitiesTestFile("singleScope.json");
        ConnectAddonBean addOn = CapabilitiesGsonFactory.getGson().fromJson(json, ConnectAddonBean.class);
        assertThat(addOn.getScopes(), is((Set<ScopeName>) newHashSet(ScopeName.READ)));
    }

    @Test
    public void multipleScopes() throws IOException
    {
        String json = readCapabilitiesTestFile("multipleScopes.json");
        ConnectAddonBean addOn = CapabilitiesGsonFactory.getGson().fromJson(json, ConnectAddonBean.class);
        assertThat(addOn.getScopes(), is((Set<ScopeName>) newHashSet(ScopeName.READ, ScopeName.WRITE)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void badScopeName() throws IOException
    {
        String json = readCapabilitiesTestFile("badScopeName.json");
        CapabilitiesGsonFactory.getGson().fromJson(json, ConnectAddonBean.class).getScopes();
    }
}
