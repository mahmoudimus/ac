package com.atlassian.plugin.remotable.host.common.descriptor;

import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.remotable.host.common.HostProperties;
import com.atlassian.plugin.remotable.host.common.util.BundleLocator;
import com.atlassian.plugin.remotable.host.common.util.RemotablePluginManifestReader;
import com.atlassian.plugin.remotable.spi.permission.PermissionsReader;
import com.atlassian.plugin.remotable.spi.util.XmlUtils;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.osgi.framework.Bundle;

import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.getOptionalAttribute;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Reads permissions from a plugin instance.  Currently it extracts the permissions from the descriptor
 * by loading the document, but soon will read natively from Plugin
 */
public class DescriptorPermissionsReader implements PermissionsReader
{
    private final Cache<Plugin,Set<String>> permissionsCache;
    private final String productKey;

    public DescriptorPermissionsReader(final HostProperties hostProperties, final BundleLocator bundleLocator)
    {
        this.productKey = hostProperties.getKey();
        this.permissionsCache = CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<Plugin,Set<String>>()
        {
            @Override
            public Set<String> load(Plugin plugin) throws Exception
            {
                return read(bundleLocator.getBundle(plugin.getKey()), productKey);
            }
        });
    }

    @Override
    public Set<String> getPermissionsForPlugin(Plugin plugin)
    {
        try
        {
            return permissionsCache.get(plugin);
        }
        catch (ExecutionException e)
        {
            // should never happen
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<String> readPermissionsFromDescriptor(Document document, InstallationMode installationMode)
    {
        return read(document, productKey, installationMode);
    }

    private Set<String> read(Bundle bundle, String productKey)
    {
        try
        {
            URL sourceUrl = bundle.getEntry("atlassian-plugin.xml");
            Document source = XmlUtils.createSecureSaxReader().read(sourceUrl);

            return read(source, productKey, RemotablePluginManifestReader.isRemotePlugin(bundle) ? InstallationMode.REMOTE : InstallationMode.LOCAL);
        }
        catch (DocumentException e)
        {
            throw new IllegalArgumentException("Unable to parse permissions from plugin descriptor", e);
        }
    }

    private Set<String> read(Document source, String productKey, InstallationMode installationMode)
    {
        Set<String> permissions = newHashSet();
        Element permissionsElement = source.getRootElement().element("plugin-info").element(
                "permissions");
        if (permissionsElement != null)
        {

            for (Element e : (List<Element>)permissionsElement.elements())
            {
                String application = getOptionalAttribute(e, "application", productKey);
                if (productKey.equals(application))
                {
                    String targetInstallationMode = getOptionalAttribute(e, "installation-mode", null);
                    if (targetInstallationMode == null || targetInstallationMode.equalsIgnoreCase(installationMode.getKey()))
                    {
                        permissions.add(e.getTextTrim());
                    }
                }
            }
        }

        String scopes = StringUtils.join(permissions, ",");
        // this number comes from the limitation in sal property settings that cannot store more
        // 255 characters in the setting's value
        if (scopes.length() > 220)
        {
            throw new PluginParseException("Cannot install remote plugin that contains too many " +
                    "permissions.");
        }

        return permissions;
    }
}
