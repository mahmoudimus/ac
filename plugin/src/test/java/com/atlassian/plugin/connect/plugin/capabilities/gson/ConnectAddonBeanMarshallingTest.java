package com.atlassian.plugin.connect.plugin.capabilities.gson;

import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.beans.BeerModule;
import com.atlassian.plugin.connect.plugin.capabilities.testobjects.beans.FoodModule;

import com.google.gson.Gson;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the basic marshalling of capability beans with our custom {@link CapabilityMapAdapterFactory}
 * 
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
        String json = "{\n" +
                "\tname: \"HipChat\",\n" +
                "\tdescription: \"Group chat and IM built for teams\",\n" +
                "\tkey: \"hipchat\",\n" +
                "\tlinks: {\n" +
                "\t\thomepage: \"https://www.hipchat.com\",\n" +
                "\t\tself: \"https://api.hipchat.com/v2/capabilities\"\n" +
                "\t},\n" +
                "\tvendor: {\n" +
                "\t\tname: \"Atlassian\",\n" +
                "\t\turl: \"http://atlassian.com\"\n" +
                "\t}\n" +
                "}";

        Gson gson = CapabilitiesGsonFactory.getGson();
        ConnectAddonBean addOn = gson.fromJson(json,ConnectAddonBean.class);

        assertEquals("HipChat",addOn.getName());
        assertEquals("Group chat and IM built for teams",addOn.getDescription());
        assertEquals("hipchat",addOn.getKey());
        assertEquals("Atlassian",addOn.getVendor().getName());
        assertEquals("http://atlassian.com",addOn.getVendor().getUrl());
        assertEquals(2,addOn.getLinks().size());
        assertEquals("https://www.hipchat.com",addOn.getLinks().get("homepage"));
        assertEquals("https://api.hipchat.com/v2/capabilities", addOn.getLinks().get("self"));
    }

    /**
     * Verifies that extra stuff in the json that an external developer may put in is ignored without exceptions
     * 
     * @throws Exception
     */
    @Test
    public void verifyExtraValuesAreIgnored() throws Exception
    {
        String json = "{\n" +
                "\tname: \"HipChat\",\n" +
                "\tsome-junk: \"wooo\",\n" +
                "\tsay: \"cheese\",\n" +
                "\tdescription: \"Group chat and IM built for teams\",\n" +
                "\tkey: \"hipchat\",\n" +
                "\tlinks: {\n" +
                "\t\thomepage: \"https://www.hipchat.com\",\n" +
                "\t\tself: \"https://api.hipchat.com/v2/capabilities\"\n" +
                "\t},\n" +
                "\tvendor: {\n" +
                "\t\tname: \"Atlassian\",\n" +
                "\t\turl: \"http://atlassian.com\"\n" +
                "\t}\n" +
                "}";

        Gson gson = CapabilitiesGsonFactory.getGson();
        ConnectAddonBean addOn = gson.fromJson(json,ConnectAddonBean.class);

        assertEquals("HipChat",addOn.getName());
        assertEquals("Group chat and IM built for teams",addOn.getDescription());
        assertEquals("hipchat",addOn.getKey());
        assertEquals("Atlassian",addOn.getVendor().getName());
        assertEquals("http://atlassian.com",addOn.getVendor().getUrl());
        assertEquals(2,addOn.getLinks().size());
        assertEquals("https://www.hipchat.com",addOn.getLinks().get("homepage"));
        assertEquals("https://api.hipchat.com/v2/capabilities", addOn.getLinks().get("self"));
    }

    /**
     * Verifies that a descriptor without a capabilities entry returns an empty capabilities map
     * 
     * @throws Exception
     */
    @Test
    public void noCapabilitiesReturnsEmptyList() throws Exception
    {
        String json = "{\n" +
                "\tname: \"HipChat\",\n" +
                "\tdescription: \"Group chat and IM built for teams\",\n" +
                "\tkey: \"hipchat\",\n" +
                "\tlinks: {\n" +
                "\t\thomepage: \"https://www.hipchat.com\",\n" +
                "\t\tself: \"https://api.hipchat.com/v2/capabilities\"\n" +
                "\t},\n" +
                "\tvendor: {\n" +
                "\t\tname: \"Atlassian\",\n" +
                "\t\turl: \"http://atlassian.com\"\n" +
                "\t}\n" +
                "}";
        
        Gson gson = CapabilitiesGsonFactory.getGson();
        ConnectAddonBean addOn = gson.fromJson(json,ConnectAddonBean.class);
        
        assertTrue(addOn.getCapabilities().isEmpty());
    }

    /**
     * Tests that a capability whose value is an object gets transformed into a list of one
     * 
     * @throws Exception
     */
    @Test
    public void singleCapabilityAsSingleObject() throws Exception
    {
        String json = "{\n" +
                "\tname: \"HipChat\",\n" +
                "\tdescription: \"Group chat and IM built for teams\",\n" +
                "\tkey: \"hipchat\",\n" +
                "\tlinks: {\n" +
                "\t\thomepage: \"https://www.hipchat.com\",\n" +
                "\t\tself: \"https://api.hipchat.com/v2/capabilities\"\n" +
                "\t},\n" +
                "\tvendor: {\n" +
                "\t\tname: \"Atlassian\",\n" +
                "\t\turl: \"http://atlassian.com\"\n" +
                "\t},\n" +
                "\tcapabilities: {\n" +
                "\t\tfood-modules: {\n" +
                "\t\t\tkey: \"a-module\",\n" +
                "\t\t\tfood: \"banana\"\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";

        Gson gson = CapabilitiesGsonFactory.getGson();
        ConnectAddonBean addOn = gson.fromJson(json,ConnectAddonBean.class);

        assertEquals(1,addOn.getCapabilities().size());
        
        List<FoodModule> moduleList = (List<FoodModule>) addOn.getCapabilities().get("food-modules");
        
        assertEquals(1,moduleList.size());
        
        FoodModule module = moduleList.get(0);
        
        assertEquals("a-module",module.getKey());
        assertEquals("banana",module.getFood());
    }

    /**
     * Verifies that a single capability type with an array of values populates a list
     * 
     * @throws Exception
     */
    @Test
    public void singleCapabilityAsList() throws Exception
    {
        String json = "{\n" +
                "\tname: \"HipChat\",\n" +
                "\tdescription: \"Group chat and IM built for teams\",\n" +
                "\tkey: \"hipchat\",\n" +
                "\tlinks: {\n" +
                "\t\thomepage: \"https://www.hipchat.com\",\n" +
                "\t\tself: \"https://api.hipchat.com/v2/capabilities\"\n" +
                "\t},\n" +
                "\tvendor: {\n" +
                "\t\tname: \"Atlassian\",\n" +
                "\t\turl: \"http://atlassian.com\"\n" +
                "\t},\n" +
                "\tcapabilities: {\n" +
                "\t\tfood-modules: [\n" +
                "\t\t\t{\n" +
                "\t\t\t\tkey: \"a-module\",\n" +
                "\t\t\t\tfood: \"banana\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\tkey: \"module-dos\",\n" +
                "\t\t\t\tfood: \"grape\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\tkey: \"module-thrice\",\n" +
                "\t\t\t\tfood: \"old socks\"\n" +
                "\t\t\t}\n" +
                "\t\t]\n" +
                "\t}\n" +
                "}";

        Gson gson = CapabilitiesGsonFactory.getGson();
        ConnectAddonBean addOn = gson.fromJson(json,ConnectAddonBean.class);

        assertEquals(1,addOn.getCapabilities().size());

        List<FoodModule> moduleList = (List<FoodModule>) addOn.getCapabilities().get("food-modules");

        assertEquals(3,moduleList.size());

        FoodModule module1 = moduleList.get(0);
        FoodModule module2 = moduleList.get(1);
        FoodModule module3 = moduleList.get(2);

        assertEquals("a-module",module1.getKey());
        assertEquals("banana",module1.getFood());

        assertEquals("module-dos",module2.getKey());
        assertEquals("grape",module2.getFood());

        assertEquals("module-thrice",module3.getKey());
        assertEquals("old socks",module3.getFood());
    }

    /**
     * Verifies that multiple capability entries with mixed object / array values is marshalled properly
     * 
     * @throws Exception
     */
    @Test
    public void multiCapabilityAsListAndObject() throws Exception
    {
        String json = "{\n" +
                "\tname: \"HipChat\",\n" +
                "\tdescription: \"Group chat and IM built for teams\",\n" +
                "\tkey: \"hipchat\",\n" +
                "\tlinks: {\n" +
                "\t\thomepage: \"https://www.hipchat.com\",\n" +
                "\t\tself: \"https://api.hipchat.com/v2/capabilities\"\n" +
                "\t},\n" +
                "\tvendor: {\n" +
                "\t\tname: \"Atlassian\",\n" +
                "\t\turl: \"http://atlassian.com\"\n" +
                "\t},\n" +
                "\tcapabilities: {\n" +
                "\t\tfood-modules: [\n" +
                "\t\t\t{\n" +
                "\t\t\t\tkey: \"a-module\",\n" +
                "\t\t\t\tfood: \"banana\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\tkey: \"module-dos\",\n" +
                "\t\t\t\tfood: \"grape\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\tkey: \"module-thrice\",\n" +
                "\t\t\t\tfood: \"old socks\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\tbeer-modules: {\n" +
                "\t\t\tkey: \"pale-ale\",\n" +
                "\t\t\tbrand: \"sierra nevada\"\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";

        Gson gson = CapabilitiesGsonFactory.getGson();
        ConnectAddonBean addOn = gson.fromJson(json,ConnectAddonBean.class);

        assertEquals(2,addOn.getCapabilities().size());

        List<FoodModule> foodList = (List<FoodModule>) addOn.getCapabilities().get("food-modules");

        assertEquals(3, foodList.size());

        FoodModule module1 = foodList.get(0);
        FoodModule module2 = foodList.get(1);
        FoodModule module3 = foodList.get(2);

        assertEquals("a-module",module1.getKey());
        assertEquals("banana",module1.getFood());

        assertEquals("module-dos",module2.getKey());
        assertEquals("grape",module2.getFood());

        assertEquals("module-thrice",module3.getKey());
        assertEquals("old socks",module3.getFood());

        List<BeerModule> beerList = (List<BeerModule>) addOn.getCapabilities().get("beer-modules");

        assertEquals(1,beerList.size());

        BeerModule beerModule = beerList.get(0);

        assertEquals("pale-ale",beerModule.getKey());
        assertEquals("sierra nevada",beerModule.getBrand());

    }
}
