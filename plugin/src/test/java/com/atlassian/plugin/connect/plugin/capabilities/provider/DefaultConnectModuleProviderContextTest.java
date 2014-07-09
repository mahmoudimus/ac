package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class DefaultConnectModuleProviderContextTest
{

    private static final String ADDON_KEY = "DasAddonz";
	private static final String TOOLS_MENU = "tools-menu";
    private ModuleLocationQualifier locationQualifier;

    @Before
    public void init()
    {
        ConnectAddonBean addon = ConnectAddonBean.newConnectAddonBean()
        		.withKey(ADDON_KEY)
                .withModules("webItems",
                        WebItemModuleBean.newWebItemBean()
                                .withKey(TOOLS_MENU)
                                .build(),
                        WebItemModuleBean.newWebItemBean()
                                .withKey("not-important")
                                .withLocation("tools-menu/first-section")
                                .build()
                )
                .withModules("webSections",
                        WebSectionModuleBean.newWebSectionBean()
                                .withKey("first-section")
                                .withLocation("tools-menu")
                                .build()
                )
                .build();

        locationQualifier = new DefaultConnectModuleProviderContext(addon).getLocationQualifier();
    }

    @Test
    public void returnsInputWhenNoMatchingKey()
    {
        final String location = "should-not-match";
        assertThat(locationQualifier.processLocation(location), is(location));
    }

    @Test
    public void returnsQualifiedKeyWhenMatchesWebItemKeyExactly()
    {
        assertThat(locationQualifier.processLocation(TOOLS_MENU), is(ADDON_KEY + 
        		ModuleKeyUtils.ADDON_MODULE_SEPARATOR + TOOLS_MENU));
    }
}