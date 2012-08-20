package com.atlassian.labs.remoteapps.plugin.module.permission;

import com.atlassian.labs.remoteapps.plugin.PermissionManager;
import com.atlassian.labs.remoteapps.plugin.product.ProductAccessor;
import com.atlassian.labs.remoteapps.plugin.settings.SettingsManager;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.user.UserManager;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 *
 */
public class TestPermissionsModuleGenerator
{
    public TestPermissionsModuleGenerator()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Mock
    PermissionManager permissionManager;

    @Mock
    ProductAccessor productAccessor;

    @Mock
    UserManager userManager;

    @Mock
    SettingsManager settingsManager;
    
    @Mock
    PluginRetrievalService pluginRetrievalService;

    @Test(expected = PluginParseException.class)
    public void testTooManyPermissions()
    {
        Element e = new DocumentFactory().createElement("permissions")
                .addElement("permission").addAttribute("scope",
                        StringUtils.rightPad("a", 220, "b")).getParent();
        new PermissionsModuleGenerator(productAccessor, userManager, settingsManager, pluginRetrievalService).validate(e, null, "foo");
    }

}
