package com.atlassian.labs.remoteapps.plugin;

import com.atlassian.labs.remoteapps.spi.InstallationFailedException;
import com.atlassian.labs.remoteapps.spi.PermissionDeniedException;
import com.atlassian.labs.remoteapps.spi.RemoteAppsService;
import com.atlassian.labs.remoteapps.plugin.installer.RemoteAppInstaller;
import com.atlassian.labs.remoteapps.plugin.installer.SchemeDelegatingRemoteAppInstaller;
import com.atlassian.labs.remoteapps.plugin.util.BundleUtil;
import com.atlassian.labs.remoteapps.plugin.util.RemoteAppManifestReader;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.sal.api.user.UserManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import static com.atlassian.labs.remoteapps.plugin.util.RemoteAppManifestReader.getInstallerUser;
import static com.atlassian.labs.remoteapps.plugin.util.RemoteAppManifestReader.isRemoteApp;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Main remote apps functions
 */
public class DefaultRemoteAppsService implements RemoteAppsService
{
    private final SchemeDelegatingRemoteAppInstaller remoteAppInstaller;
    private final UserManager userManager;
    private final BundleContext bundleContext;
    private final PermissionManager permissionManager;
    private final PluginController pluginController;
    private final PluginAccessor pluginAccessor;
    private static final Logger log = LoggerFactory.getLogger(DefaultRemoteAppsService.class);

    public DefaultRemoteAppsService(SchemeDelegatingRemoteAppInstaller remoteAppInstaller, UserManager userManager,
            BundleContext bundleContext, PermissionManager permissionManager,
            PluginController pluginController,
            PluginAccessor pluginAccessor)
    {
        this.remoteAppInstaller = remoteAppInstaller;
        this.userManager = userManager;
        this.bundleContext = bundleContext;
        this.permissionManager = permissionManager;
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public String install(final String username, String registrationUrl, String registrationSecret,
            boolean stripUnknownModules) throws
                                                                                            PermissionDeniedException,
                                                                                            InstallationFailedException
    {
        validateCanInstall(username);

        URI parsedRegistrationUri;
        try
        {
            parsedRegistrationUri = new URI(registrationUrl);
        }
        catch (URISyntaxException e)
        {
            throw new InstallationFailedException("Invalid URI: '" + registrationUrl + "'");
        }
        try
        {
            String appKey = remoteAppInstaller.install(username, parsedRegistrationUri, registrationSecret,
                    stripUnknownModules, new RemoteAppInstaller.KeyValidator()
               {
                   @Override
                   public void validatePermissions(String appKey) throws PermissionDeniedException
                   {
                       if (doesAppExist(appKey))
                       {
                           validateCanAuthor(username, appKey);
                       }
                   }
               });
            log.info("Remote app '{}' installed by '{}' successfully", appKey, username);
            return appKey;
        }
        catch (PermissionDeniedException ex)
        {
            log.warn("Permission denied for installation of '" + registrationUrl + "'" +
                             " by user '" + username + "'", ex);
            throw ex;
        }
        catch (InstallationFailedException ex)
        {
            log.warn("Installation failed for registration URL '" + registrationUrl + "'" +
                             " and user '" + username + "'", ex);
            throw ex;
        }
        catch (RuntimeException ex)
        {
            log.warn("Installation failed for registration URL '" + registrationUrl + "'" +
                             " and user '" + username + "'", ex);
            throw new InstallationFailedException(ex);
        }

    }

    @Override
    public void uninstall(String username, String appKey) throws PermissionDeniedException
    {
        validateCanInstall(username);
        validateAppExists(appKey);
        validateCanAuthor(username, appKey);
        pluginController.uninstall(pluginAccessor.getPlugin(appKey));
        log.info("Remote app '{}' uninstalled by '{}' successfully", appKey, username);
    }

    @Override
    public Set<String> reinstallRemotePlugins(String remoteUsername)
    {
        if (!userManager.isSystemAdmin(remoteUsername))
        {
            throw new PermissionDeniedException("Only system administrators are allowed to reinstall "
                + "all remote plugins");
        }

        Set<String> reinstalledKeys = newHashSet();
        for (Plugin plugin : pluginAccessor.getPlugins())
        {
            try
            {
                Bundle bundle = BundleUtil.findBundleForPlugin(bundleContext, plugin.getKey());
                if (bundle != null && isRemoteApp(bundle))
                {
                    String registrationUri = RemoteAppManifestReader.getRegistrationUrl(bundle);
                    reinstalledKeys.add(install(remoteUsername, registrationUri, "", false));
                }
            }
            catch (Exception ex)
            {
                log.warn("Unable to reinstall remote plugin " + plugin.getKey(), ex);
            }
        }
        return reinstalledKeys;
    }

    private void validateCanAuthor(String username, String appKey)
    {
        if (!(username.equals(getInstallerUser(BundleUtil.findBundleForPlugin(bundleContext, appKey))) ||
            userManager.isSystemAdmin(username)))
        {
            throw new PermissionDeniedException("Unauthorized uninstallation from '" + username + "'. " +
                "Must be the author or a system administrator.");
        }
    }

    private boolean doesAppExist(String appKey)
    {
        Bundle bundle = BundleUtil.findBundleForPlugin(bundleContext, appKey);
        return bundle != null && isRemoteApp(bundle);
    }

    private void validateAppExists(String appKey)
    {
        if (!doesAppExist(appKey))
        {
            throw new PermissionDeniedException("Remote app '" + appKey + "' doesn't exist");
        }
    }

    private void validateCanInstall(String username)
    {
        if (!permissionManager.canInstallRemoteApps(username))
        {
            throw new PermissionDeniedException("Unauthorized access by '" + username + "'");
        }
    }
}
