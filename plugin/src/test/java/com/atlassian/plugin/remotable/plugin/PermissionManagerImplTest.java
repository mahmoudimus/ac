package com.atlassian.plugin.remotable.plugin;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.remotable.plugin.settings.SettingsManager;
import com.atlassian.plugin.remotable.spi.permission.Permission;
import com.atlassian.plugin.remotable.spi.permission.PermissionModuleDescriptor;
import com.atlassian.plugin.remotable.spi.permission.PermissionsReader;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.Set;

import static com.atlassian.plugin.remotable.api.InstallationMode.LOCAL;
import static com.atlassian.plugin.remotable.api.InstallationMode.REMOTE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PermissionManagerImplTest
{
    private PermissionManager permissionManager;

    @Mock
    private UserManager userManager;

    @Mock
    private SettingsManager settingsManager;

    @Mock
    private PluginAccessor pluginAccessor;

    @Mock
    private PluginEventManager pluginEventManager;

    @Mock
    private PermissionsReader permissionsReader;

    @Mock
    private BundleContext bundleContext;

    @Mock
    private PluginModuleTracker<Permission, PermissionModuleDescriptor> pluginModuleTracker;

    @Before
    public void setUp()
    {
        permissionManager = new PermissionManagerImpl(
                userManager, settingsManager, pluginAccessor,
                permissionsReader, bundleContext, pluginModuleTracker);
    }

    @Test
    public void testGetPermissions() throws Exception
    {
        final Permission p1 = mock(Permission.class);
        when(p1.getInstallationModes()).thenReturn(ImmutableSet.of(LOCAL));

        final Permission p2 = mock(Permission.class);
        when(p2.getInstallationModes()).thenReturn(ImmutableSet.of(REMOTE));

        when(pluginModuleTracker.getModules()).thenReturn(ImmutableList.of(p1, p2));
        Set<Permission> permissions = permissionManager.getPermissions(LOCAL);

        assertTrue(permissions.contains(p1));
        assertFalse(permissions.contains(p2));
    }
}
