package com.atlassian.labs.remoteapps.modules.permissions;

import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScopeSchema;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.labs.remoteapps.settings.SettingsManager;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.sal.api.user.UserManager;
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

    private static final Element NON_EMPTY_PERMISSIONS = new DocumentFactory().createElement("permissions")
            .addElement("permission").getParent();

    @Test
    public void testAllowNonAdminsForDogfood()
    {
        when(userManager.isAdmin("foo")).thenReturn(false);
        when(settingsManager.isAllowDogfooding()).thenReturn(true);
        new PermissionsModuleGenerator(permissionManager, productAccessor, userManager, settingsManager, apiScopeSchema).validate(NON_EMPTY_PERMISSIONS, "", "foo");
    }

    @Test
    public void testAllowAdminsForNonDogfood()
    {
        when(userManager.isAdmin("foo")).thenReturn(true);
        when(settingsManager.isAllowDogfooding()).thenReturn(false);
        new PermissionsModuleGenerator(permissionManager, productAccessor, userManager, settingsManager, apiScopeSchema).validate(NON_EMPTY_PERMISSIONS, "", "foo");
    }

    @Test(expected = PluginParseException.class)
    public void testDisallowNonAdminsForNonDogfood()
    {
        when(userManager.isAdmin("foo")).thenReturn(false);
        when(settingsManager.isAllowDogfooding()).thenReturn(false);
        new PermissionsModuleGenerator(permissionManager, productAccessor, userManager, settingsManager, apiScopeSchema).validate(NON_EMPTY_PERMISSIONS, "", "foo");
    }

}
