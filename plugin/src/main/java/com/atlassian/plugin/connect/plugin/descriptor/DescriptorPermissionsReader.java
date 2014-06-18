package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.util.BundleLocator;
import com.atlassian.plugin.connect.plugin.xmldescriptor.XmlDescriptorExploder;
import com.atlassian.plugin.connect.spi.Filenames;
import com.atlassian.plugin.connect.spi.host.HostProperties;
import com.atlassian.plugin.connect.spi.permission.PermissionsReader;
import com.atlassian.plugin.connect.spi.util.XmlUtils;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.osgi.framework.Bundle;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.getOptionalAttribute;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Reads permissions from a plugin instance.  Currently it extracts the permissions from the descriptor
 * by loading the document, but soon will read natively from Plugin
 */
@ExportAsService(PermissionsReader.class)
@XmlDescriptor
@Named
public final class DescriptorPermissionsReader implements PermissionsReader
{
    private final Cache<Plugin,Set<String>> permissionsCache;
    private final String productKey;

    @Inject
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
        XmlDescriptorExploder.notifyAndExplode(null == plugin ? null : plugin.getKey());

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
    public Set<String> readPermissionsFromDescriptor(Document document)
    {
        return read(document, productKey);
    }

    private Set<String> read(Bundle bundle, String productKey)
    {
        try
        {
            URL sourceUrl = bundle.getEntry(Filenames.ATLASSIAN_PLUGIN_XML);
            Document source = XmlUtils.createSecureSaxReader().read(sourceUrl);

            return read(source, productKey);
        }
        catch (DocumentException e)
        {
            throw new IllegalArgumentException("Unable to parse permissions from plugin descriptor", e);
        }
    }

    private Set<String> read(Document source, String productKey)
    {
        XmlDescriptorExploder.notifyAndExplode(null == source ? null : source.getRootElement().attributeValue("key"));

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

                        permissions.add(e.getTextTrim());
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
