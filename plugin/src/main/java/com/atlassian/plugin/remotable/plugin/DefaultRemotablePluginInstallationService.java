package com.atlassian.plugin.remotable.plugin;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Response;
import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.remotable.host.common.util.FormatConverter;
import com.atlassian.plugin.remotable.host.common.util.RemotablePluginManifestReader;
import com.atlassian.plugin.remotable.plugin.descriptor.DescriptorValidator;
import com.atlassian.plugin.remotable.plugin.installer.RemotePluginInstaller;
import com.atlassian.plugin.remotable.spi.InstallationFailedException;
import com.atlassian.plugin.remotable.spi.PermissionDeniedException;
import com.atlassian.plugin.remotable.spi.RemotablePluginInstallationService;
import com.atlassian.plugin.remotable.host.common.util.BundleUtil;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Function;
import org.dom4j.Document;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import static com.atlassian.plugin.remotable.host.common.util.RemotablePluginManifestReader
        .isRemotePlugin;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Main remotable plugins functions
 */
public class DefaultRemotablePluginInstallationService implements RemotablePluginInstallationService
{
    private final RemotePluginInstaller remotePluginInstaller;
    private final UserManager userManager;
    private final BundleContext bundleContext;
    private final PermissionManager permissionManager;
    private final PluginController pluginController;
    private final PluginAccessor pluginAccessor;
    private final HttpClient httpClient;
    private final FormatConverter formatConverter;
    private final DescriptorValidator descriptorValidator;
    private static final Logger log = LoggerFactory.getLogger(
            DefaultRemotablePluginInstallationService.class);

    public DefaultRemotablePluginInstallationService(
            RemotePluginInstaller remotePluginInstaller,
            UserManager userManager,
            BundleContext bundleContext,
            PermissionManager permissionManager,
            PluginController pluginController,
            PluginAccessor pluginAccessor,
            HttpClient httpClient, FormatConverter formatConverter,
            DescriptorValidator descriptorValidator)
    {
        this.remotePluginInstaller = remotePluginInstaller;
        this.userManager = userManager;
        this.bundleContext = bundleContext;
        this.permissionManager = permissionManager;
        this.pluginController = pluginController;
        this.pluginAccessor = pluginAccessor;
        this.httpClient = httpClient;
        this.formatConverter = formatConverter;
        this.descriptorValidator = descriptorValidator;
    }

    @Override
    public String install(final String username, String registrationUrl) throws
            PermissionDeniedException,
            InstallationFailedException
    {
        URI parsedRegistrationUri;
        try
        {
            parsedRegistrationUri = new URI(registrationUrl);
        }
        catch (URISyntaxException e)
        {
            throw new InstallationFailedException("Invalid URI: '" + registrationUrl + "'");
        }

        Document pluginDescriptor = getPluginDescriptor(registrationUrl);

        validateCanInstallPlugins(username);

        String pluginKey = pluginDescriptor.getRootElement().attributeValue("key");
        validateCanEditPluginIfExists(username, pluginKey);

        validateDeclaredPermissionsCanBeRequested(username, pluginDescriptor);

        validatePluginKeyIsInstallable(username, pluginKey);

        try
        {
            String appKey = remotePluginInstaller.install(username, parsedRegistrationUri,
                    pluginDescriptor);
            log.info("Remote plugin '{}' installed by '{}' successfully", appKey, username);
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

    private void validatePluginKeyIsInstallable(String userName, String pluginKey)
    {
        if (!permissionManager.isRemotePluginInstallable(userName, pluginKey))
        {
            throw new PermissionDeniedException("Plugin key '" + pluginKey + "' isn't installable remotely");
        }
    }

    private void validateCanInstallPlugins(String username)
    {
        if (!permissionManager.canInstallRemotePlugins(username))
        {
            throw new PermissionDeniedException("Unauthorized access by '" + username + "'");
        }
    }

    private void validateCanEditPluginIfExists(String username, String pluginKey)
    {
        if (doesAppExist(pluginKey) && !permissionManager.canModifyRemotePlugin(username, pluginKey))
        {
            throw new PermissionDeniedException("Unauthorized modification of plugin '" + pluginKey + "' by '" + username + "'");

        }
    }

    private void validateDeclaredPermissionsCanBeRequested(String username, Document descriptor)
    {
        if (!permissionManager.canRequestDeclaredPermissions(username, descriptor, InstallationMode.REMOTE))
        {
            throw new PermissionDeniedException("Unauthorized request of permissions by '" + username + "'");

        }
    }

    @Override
    public void uninstall(String username, String appKey) throws PermissionDeniedException
    {
        validateAppExists(appKey);
        validateCanInstallPlugins(username);
        validateCanEditPluginIfExists(username, appKey);
        pluginController.uninstall(pluginAccessor.getPlugin(appKey));
        log.info("Remote plugin '{}' uninstalled by '{}' successfully", appKey, username);
    }

    @Override
    public String getPluginKey(final String registrationUrl)
    {
        return getPluginDescriptor(registrationUrl).getRootElement().attributeValue("key");
    }

    private Document getPluginDescriptor(final String registrationUrl)
    {
        return httpClient.newRequest(registrationUrl)
                .get()
                .<Document>transform()
                .ok(new Function<Response, Document>()
                {
                    @Override
                    public Document apply(Response response)
                    {
                        Document document = formatConverter.toDocument(registrationUrl.toString(),
                                response.getHeader("Content-Type"),
                                response.getEntity());
                        descriptorValidator.validate(URI.create(registrationUrl), document);
                        return document;
                    }
                })
                .others(new Function<Response, Document>()
                {
                    @Override
                    public Document apply(Response response)
                    {
                        throw new InstallationFailedException(
                                "Unable to retrieve the descriptor (" + response
                                        .getStatusCode() + ") - " + response
                                        .getStatusText());
                    }
                })
                .fail(new Function<Throwable, Document>()
                {
                    @Override
                    public Document apply(@Nullable Throwable input)
                    {
                        log.debug("Error retrieving descriptor", input);
                        throw new InstallationFailedException("Unable to contact and retrieve " +
                                "descriptor from " + registrationUrl);
                    }
                })
                .claim();
    }

    @Override
    public Set<String> reinstallRemotePlugins(String remoteUsername)
    {
        if (!userManager.isSystemAdmin(remoteUsername))
        {
            throw new PermissionDeniedException(
                    "Only system administrators are allowed to reinstall "
                            + "all remote plugins");
        }

        Set<String> reinstalledKeys = newHashSet();
        for (Plugin plugin : pluginAccessor.getPlugins())
        {
            try
            {
                Bundle bundle = BundleUtil.findBundleForPlugin(bundleContext, plugin.getKey());
                if (bundle != null && isRemotePlugin(bundle))
                {
                    String registrationUri = RemotablePluginManifestReader.getRegistrationUrl(
                            bundle);
                    reinstalledKeys.add(install(remoteUsername, registrationUri));
                }
            }
            catch (Exception ex)
            {
                log.warn("Unable to reinstall remote plugin " + plugin.getKey(), ex);
            }
        }
        return reinstalledKeys;
    }

    private boolean doesAppExist(String appKey)
    {
        Bundle bundle = BundleUtil.findBundleForPlugin(bundleContext, appKey);
        return bundle != null && isRemotePlugin(bundle);
    }

    private void validateAppExists(String appKey)
    {
        if (!doesAppExist(appKey))
        {
            throw new PermissionDeniedException(appKey,
                    "Remote plugin '" + appKey + "' doesn't exist");
        }
    }
}
