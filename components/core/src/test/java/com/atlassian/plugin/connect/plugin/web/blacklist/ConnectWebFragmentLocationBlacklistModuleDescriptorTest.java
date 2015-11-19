package com.atlassian.plugin.connect.plugin.web.blacklist;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.web.blacklist.ConnectWebFragmentLocationBlacklistModuleDescriptor.ConnectWebFragmentLocationBlacklist;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ConnectWebFragmentLocationBlacklistModuleDescriptorTest
{
    public static final String WEB_ITEM_BLACKLISTED_LOCATION = "web-item-blacklisted-location";
    public static final String WEB_PANEL_BLACKLISTED_LOCATION = "web-panel-blacklisted-location";

    private ConnectWebFragmentLocationBlacklistModuleDescriptor moduleDescriptor;

    @Before
    public void setUp() throws Exception
    {
        ModuleFactory moduleFactory = mock(ModuleFactory.class);
        moduleDescriptor = new ConnectWebFragmentLocationBlacklistModuleDescriptor(moduleFactory);
    }

    @Test
    public void testLoadingBlacklist()
    {
        DefaultElement descriptorXML = new DefaultElement("connect-web-fragment-location-blacklist");

        descriptorXML.add(elementWithLocations("web-panel-locations", WEB_PANEL_BLACKLISTED_LOCATION));
        descriptorXML.add(elementWithLocations("web-item-locations", WEB_ITEM_BLACKLISTED_LOCATION));
        descriptorXML.addAttribute("key", "module-key");

        moduleDescriptor.init(mock(Plugin.class), descriptorXML);

        ConnectWebFragmentLocationBlacklist blacklist = moduleDescriptor.getModule();

        assertThat(blacklist.getWebItemBlacklistedLocations(), Matchers.contains(WEB_ITEM_BLACKLISTED_LOCATION));
        assertThat(blacklist.getWebPanelBlacklistedLocations(), Matchers.contains(WEB_PANEL_BLACKLISTED_LOCATION));
    }

    private Element elementWithLocations(String elementName, String... locations)
    {
        Element elementWithLocations = new DefaultElement(elementName);
        for (String location : locations)
        {
            elementWithLocations.add(locationElement(location));
        }
        return elementWithLocations;
    }

    private Element locationElement(String location)
    {
        DefaultElement locationElement = new DefaultElement("location");
        locationElement.add(new DefaultText(location));
        return locationElement;
    }
}