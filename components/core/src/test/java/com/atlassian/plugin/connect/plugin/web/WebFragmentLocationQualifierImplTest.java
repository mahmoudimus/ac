package com.atlassian.plugin.connect.plugin.web;

import com.atlassian.plugin.connect.api.web.WebFragmentLocationQualifier;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class WebFragmentLocationQualifierImplTest {

    private static final String ADDON_KEY = "DasAddonz";
    private static final String PRODUCT_LOCATION = "tools-menu";
    private static final String WEB_SECTION_KEY = "first-section";

    private WebFragmentLocationQualifier locationQualifier;
    private ConnectAddonBean addon;

    @Before
    public void setUp() {
        addon = ConnectAddonBean.newConnectAddonBean()
                .withKey(ADDON_KEY)
                .withModules("webItems",
                        WebItemModuleBean.newWebItemBean()
                                .withKey(PRODUCT_LOCATION)
                                .build(),
                        WebItemModuleBean.newWebItemBean()
                                .withKey("some-key")
                                .withLocation(PRODUCT_LOCATION + "/" + WEB_SECTION_KEY)
                                .build()
                )
                .withModules("webSections",
                        WebSectionModuleBean.newWebSectionBean()
                                .withKey(WEB_SECTION_KEY)
                                .withLocation("tools-menu")
                                .build()
                )
                .build();

        locationQualifier = new WebFragmentLocationQualifierImpl();
    }

    @Test
    public void returnsInputWhenNoMatchingKey() {
        final String location = "should-not-match";
        assertThat(locationQualifier.processLocation(location, addon), is(location));
    }

    @Test
    public void returnsQualifiedKeyWhenMatchesWebItemKeyExactly() {
        assertThat(locationQualifier.processLocation(PRODUCT_LOCATION, addon), is(qualify(PRODUCT_LOCATION)));
    }

    @Test
    public void substitutesAllKeysInLocation() {
        assertThat(locationQualifier.processLocation(PRODUCT_LOCATION + '/' + WEB_SECTION_KEY, addon),
                is(qualify(PRODUCT_LOCATION) + '/' + qualify(WEB_SECTION_KEY)));
    }

    @Test
    public void substitutesOnlyKeysInLocation() {
        final String segment = "should-not-be-qualified";

        assertThat(locationQualifier.processLocation(PRODUCT_LOCATION + '/' + segment, addon),
                is(qualify(PRODUCT_LOCATION) + '/' + segment));
    }

    private String qualify(String str) {
        return ModuleKeyUtils.addonAndModuleKey(ADDON_KEY, str);
    }
}
