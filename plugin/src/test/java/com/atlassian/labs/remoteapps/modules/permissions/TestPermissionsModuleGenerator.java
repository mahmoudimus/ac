package com.atlassian.labs.remoteapps.modules.permissions;

import com.atlassian.labs.remoteapps.ApplicationLinkAccessor;
import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScopeSchema;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.labs.remoteapps.settings.SettingsManager;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.sal.api.user.UserManager;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

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
    ApiScopeSchema apiScopeSchema;

    @Mock
    ApplicationLinkAccessor applicationLinkAccessor;

    private static final Element NON_EMPTY_PERMISSIONS = new DocumentFactory().createElement("permissions")
            .addElement("permission").addAttribute("scope", "foo").getParent();

    @Test
    public void testAllowNonSysAdminsForDogfood()
    {
        when(userManager.isSystemAdmin("foo")).thenReturn(false);
        when(settingsManager.isAllowDogfooding()).thenReturn(true);
        new PermissionsModuleGenerator(permissionManager, productAccessor, userManager, settingsManager, apiScopeSchema,
                applicationLinkAccessor).validate(NON_EMPTY_PERMISSIONS, null, "foo");
    }

    @Test
    public void testAllowSysAdminsForNonDogfood()
    {
        when(userManager.isSystemAdmin("foo")).thenReturn(true);
        when(settingsManager.isAllowDogfooding()).thenReturn(false);
        new PermissionsModuleGenerator(permissionManager, productAccessor, userManager, settingsManager, apiScopeSchema,
                applicationLinkAccessor).validate(NON_EMPTY_PERMISSIONS, null, "foo");
    }

    @Test(expected = PluginParseException.class)
    public void testDisallowNonAdminsForNonDogfood()
    {
        when(userManager.isAdmin("foo")).thenReturn(false);
        when(settingsManager.isAllowDogfooding()).thenReturn(false);
        new PermissionsModuleGenerator(permissionManager, productAccessor, userManager, settingsManager, apiScopeSchema,
                applicationLinkAccessor).validate(NON_EMPTY_PERMISSIONS, null, "foo");
    }

    @Test(expected = PluginParseException.class)
    public void testTooManyPermissions()
    {
        Element e = new DocumentFactory().createElement("permissions")
                .addElement("permission").addAttribute("scope",
                        StringUtils.rightPad("a", 220, "b")).getParent();
        new PermissionsModuleGenerator(permissionManager, productAccessor, userManager, settingsManager, apiScopeSchema,
                applicationLinkAccessor).validate(e, null, "foo");
    }

}
