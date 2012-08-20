package com.atlassian.labs.remoteapps.plugin.module.permission;

import com.atlassian.labs.remoteapps.api.InstallationMode;
import com.atlassian.labs.remoteapps.spi.util.XmlUtils;
import com.atlassian.labs.remoteapps.plugin.product.ProductAccessor;
import com.atlassian.labs.remoteapps.plugin.util.BundleUtil;
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
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static com.atlassian.labs.remoteapps.plugin.util.RemoteAppManifestReader.isRemoteApp;
import static com.atlassian.labs.remoteapps.spi.util.Dom4jUtils.getOptionalAttribute;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Reads permissions from a plugin instance.  Currently it extracts the permissions from the descriptor
 * by loading the document, but soon will read natively from Plugin
 */
@Component
public class PermissionsReader
{
    private final Cache<Plugin,Set<String>> permissionsCache;
    private final BundleContext bundleContext;
    private final ProductAccessor productAccessor;

    @Autowired
    public PermissionsReader(final ProductAccessor productAccessor, BundleContext bundleContext)
    {
        this.bundleContext = bundleContext;
        this.productAccessor = productAccessor;
        this.permissionsCache = CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<Plugin,Set<String>>()
        {
            @Override
            public Set<String> load(Plugin plugin) throws Exception
            {
                return read(plugin, productAccessor);
            }
        });
    }

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

    public Set<String> readPermissionsFromDescriptor(Document document, InstallationMode installationMode)
    {
        return read(document, productAccessor, installationMode);
    }

    private Set<String> read(Plugin plugin, ProductAccessor productAccessor)
    {
        try
        {
            Bundle bundle = BundleUtil.findBundleForPlugin(bundleContext, plugin.getKey());
            URL sourceUrl = bundle.getEntry("atlassian-plugin.xml");
            Document source = XmlUtils.createSecureSaxReader().read(sourceUrl);

            return read(source, productAccessor, isRemoteApp(bundle) ? InstallationMode.REMOTE : InstallationMode.LOCAL);
        }
        catch (DocumentException e)
        {
            throw new IllegalArgumentException("Unable to parse permissions from plugin descriptor", e);
        }
    }

    private Set<String> read(Document source, ProductAccessor productAccessor, InstallationMode installationMode)
    {
        Set<String> permissions = newHashSet();
        Element permissionsElement = source.getRootElement().element("plugin-info").element(
                "permissions");
        if (permissionsElement != null)
        {

            for (Element e : (List<Element>)permissionsElement.elements())
            {
                String application = getOptionalAttribute(e, "application", productAccessor.getKey());
                if (productAccessor.getKey().equals(application))
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
            throw new PluginParseException("Cannot install remote app that contains too many " +
                    "permissions.");
        }

        return permissions;
    }
}
