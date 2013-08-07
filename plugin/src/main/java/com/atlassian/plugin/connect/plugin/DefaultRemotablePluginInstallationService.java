package com.atlassian.plugin.connect.plugin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Response;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginController;
import com.atlassian.plugin.connect.plugin.descriptor.DescriptorValidator;
import com.atlassian.plugin.connect.plugin.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.plugin.descriptor.util.FormatConverter;
import com.atlassian.plugin.connect.plugin.installer.RemotePluginInstaller;
import com.atlassian.plugin.connect.plugin.util.BundleUtil;
import com.atlassian.plugin.connect.plugin.util.RemotablePluginManifestReader;
import com.atlassian.plugin.connect.spi.InstallationFailedException;
import com.atlassian.plugin.connect.spi.PermissionDeniedException;
import com.atlassian.plugin.connect.spi.RemotablePluginInstallationService;
import com.atlassian.sal.api.user.UserManager;

import com.google.common.base.Function;

import org.dom4j.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.plugin.connect.plugin.util.RemotablePluginManifestReader.isRemotePlugin;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;

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

        validateCanInstallArbitraryPlugins(username);

        Document pluginDescriptor = getPluginDescriptor(registrationUrl);


        String pluginKey = pluginDescriptor.getRootElement().attributeValue("key");
        validateCanEditPluginIfExists(username, pluginKey);

        validateDeclaredPermissionsCanBeRequested(username, pluginDescriptor);

        return installPlugin(username, parsedRegistrationUri, pluginDescriptor);

    }

    @Override
    public String installFromMarketplace(final String username, String pluginKey) throws
            PermissionDeniedException,
            InstallationFailedException
    {

        validateCanInstallPluginsFromMarketplace(username);

        String pluginDescriptorUrl = getPluginDescriptorUrl(pluginKey);

        Document pluginDescriptor = getPluginDescriptor(pluginDescriptorUrl);

        return installPlugin(username, URI.create(pluginDescriptorUrl), pluginDescriptor);

    }

    private String installPlugin(String username, URI parsedRegistrationUri, Document pluginDescriptor)
    {
        try
        {
            String appKey = remotePluginInstaller.install(username, parsedRegistrationUri,
                    pluginDescriptor);
            log.info("Remote plugin '{}' installed by '{}' successfully", appKey, username);
            return appKey;
        }
        catch (PermissionDeniedException ex)
        {
            log.warn("Permission denied for installation of '" + parsedRegistrationUri + "'" +
                    " by user '" + username + "'", ex);
            throw ex;
        }
        catch (InstallationFailedException ex)
        {
            log.warn("Installation failed for registration URL '" + parsedRegistrationUri + "'" +
                    " and user '" + username + "'", ex);
            throw ex;
        }
        catch (RuntimeException ex)
        {
            log.warn("Installation failed for registration URL '" + parsedRegistrationUri + "'" +
                    " and user '" + username + "'", ex);
            throw new InstallationFailedException(ex);
        }
    }

    private void validateCanInstallArbitraryPlugins(String username)
    {
        if (!permissionManager.canInstallArbitraryRemotePlugins(username))
        {
            throw new PermissionDeniedException("User '" + username + "' cannot install arbitrary plugins");
        }
    }

    private void validateCanInstallPluginsFromMarketplace(String username)
    {
        if (!permissionManager.canInstallRemotePluginsFromMarketplace(username))
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
        if (!permissionManager.canRequestDeclaredPermissions(username, descriptor))
        {
            throw new PermissionDeniedException("Unauthorized request of permissions by '" + username + "'");
        }
    }

    @Override
    public void uninstall(String username, String appKey) throws PermissionDeniedException
    {
        validateAppExists(appKey);
        validateCanInstallPluginsFromMarketplace(username);
        validateCanEditPluginIfExists(username, appKey);
        pluginController.uninstall(pluginAccessor.getPlugin(appKey));
        log.info("Remote plugin '{}' uninstalled by '{}' successfully", appKey, username);
    }

    @Override
    public String getPluginKey(final String registrationUrl)
    {
        return getPluginDescriptor(registrationUrl).getRootElement().attributeValue("key");
    }

    Document getPluginDescriptor(final String registrationUrl)
    {
        return httpClient.newRequest(registrationUrl)
                .setHeader("Accept", "application/xml")
                .get()
                .<Document>transform()
                .ok(new Function<Response, Document>()
                {
                    @Override
                    public Document apply(Response response)
                    {
                        try
                        {
                            Document document = formatConverter.toDocument(registrationUrl,
                                    response.getHeader("Content-Type"),
                                    response.getEntity());
                            descriptorValidator.validate(URI.create(registrationUrl), document);
                            return document;
                        }
                        catch (InvalidDescriptorException e) {
                            throw new InstallationFailedException(e);
                        }
                            
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
                    public Document apply(Throwable input)
                    {
                        log.debug("Error retrieving descriptor", input);
                        throw new InstallationFailedException(
                                format("Unable to contact and retrieve descriptor from '%s', message is: %s", registrationUrl, input.getMessage()),
                                input);
                    }
                })
                .claim();
    }

    String getPluginDescriptorUrl(final String pluginKey)
    {
        String baseurl = System.getProperty("mpac.baseurl", "https://marketplace.atlassian.com");
        return httpClient.newRequest(baseurl + "/rest/1.0/plugins/" + pluginKey)
                .get()
                .<String>transform()
                .ok(new Function<Response, String>()
                {
                    @Override
                    public String apply(Response input)
                    {
                        try
                        {
                            JSONObject object = new JSONObject(input.getEntity());
                            JSONObject version = object.getJSONObject("version");
                            if (version == null || !isValidPluginSystemVersion(version))
                            {
                                throw new InstallationFailedException("Unable to find valid plugin version for key " + pluginKey);
                            }
                            return findDescriptorUrl(version);
                        }
                        catch (JSONException e)
                        {
                            throw new InstallationFailedException("Unable to parse marketplace response for key " + pluginKey, e);
                        }
                    }
                })
                .otherwise(new Function<Throwable, String>()
                {
                    @Override
                    public String apply(Throwable input)
                    {
                        throw new InstallationFailedException("Error retrieving response from marketplace for key " + pluginKey, input);
                    }
                })
                .claim();
    }

    private String findUrl(JSONArray links, String rel) throws JSONException
    {
        for (int x=0; x< links.length(); x++)
        {
            final JSONObject link = links.getJSONObject(x);
            if (rel.equals(link.getString("rel")))
            {
                return link.getString("href");
            }
        }
        return null;
    }

    public String findDescriptorUrl(JSONObject version) throws JSONException
    {
        JSONArray links = version.getJSONArray("links");
        String url = findUrl(links, "descriptor");
        // @todo remove "binary" fallback when MPAC implements descriptor hosting?
        // we really want to use the "descriptor" url but it's NYI on MPAC, so fall back to "binary" if necessary
        if (url == null) url = findUrl(links, "binary");
        if (url == null) throw new JSONException("No descriptor url found for descriptor");
        return url;
    }

    private boolean isValidPluginSystemVersion(JSONObject version) throws JSONException
    {
        if (version != null)
        {
            String type = version.getString("addOnType");
            // @todo is this a bug in mpac?  addOnType in json is "Plugins 3", but query param input requires "three"
            return "Plugins 3".equalsIgnoreCase(type) || "three".equalsIgnoreCase(type);
        }
        return false;
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
