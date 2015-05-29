package com.atlassian.plugin.connect.core.capabilities.provider;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.spi.module.ModuleLocationQualifier;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class DefaultModuleLocationQualifierTest
{

    private static final String ADDON_KEY = "DasAddonz";
    private static final String TOOLS_MENU = "tools-menu";
    private static final String WEB_SECTION_KEY = "first-section";
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
                                .withKey(WEB_SECTION_KEY)
                                .withLocation("tools-menu")
                                .build()
                )
                .build();

        locationQualifier = new DefaultModuleLocationQualifier(addon);
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
        assertThat(locationQualifier.processLocation(TOOLS_MENU), is(qualify(TOOLS_MENU)));
    }

    @Test
    public void substitutesAllKeysInLocation()
    {
        assertThat(locationQualifier.processLocation(TOOLS_MENU + '/' + WEB_SECTION_KEY),
                is(qualify(TOOLS_MENU) + '/' + qualify(WEB_SECTION_KEY)));
    }

    @Test
    public void substitutesOnlyKeysInLocation()
    {
        final String segment = "should-not-be-qualified";

        assertThat(locationQualifier.processLocation(TOOLS_MENU + '/' + segment),
                is(qualify(TOOLS_MENU) + '/' + segment));
    }

    private String qualify(String str)
    {
        return ModuleKeyUtils.addonAndModuleKey(ADDON_KEY, str);
    }
}