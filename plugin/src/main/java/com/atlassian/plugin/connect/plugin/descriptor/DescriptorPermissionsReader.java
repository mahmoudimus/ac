package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.gson.CapabilitiesGsonFactory;
import com.atlassian.plugin.connect.plugin.util.BundleLocator;
import com.atlassian.plugin.connect.spi.host.HostProperties;
import com.atlassian.plugin.connect.spi.permission.PermissionsReader;
import com.atlassian.plugin.connect.spi.util.XmlUtils;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.opensymphony.util.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
@Named
public final class DescriptorPermissionsReader implements PermissionsReader
{
    private final Cache<Plugin,Set<String>> permissionsCache;
    private final Cache<Plugin,Set<String>> scopesCache;
    private final String productKey;

    private static final Logger log = LoggerFactory.getLogger(DescriptorPermissionsReader.class);
    private static final String ATLASSIAN_PLUGIN_XML = "atlassian-plugin.xml";
    private static final String ATLASSIAN_ADD_ON_JSON = "atlassian-add-on.json";

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
        this.scopesCache = CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<Plugin,Set<String>>()
        {
            @Override
            public Set<String> load(Plugin plugin) throws Exception
            {
                return readScopes(bundleLocator.getBundle(plugin.getKey()), productKey);
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
    public Set<String> readPermissionsFromDescriptor(Document document)
    {
        return read(document, productKey);
    }

    @Override
    public Set<String> readScopesForAddOn(Plugin plugin)
    {
        try
        {
            return scopesCache.get(plugin);
        }
        catch (ExecutionException e)
        {
            // should never happen
            throw new RuntimeException(e);
        }
    }

    private Set<String> read(Bundle bundle, String productKey)
    {
        try
        {
            URL sourceUrl = bundle.getEntry(ATLASSIAN_PLUGIN_XML);
            Document source = XmlUtils.createSecureSaxReader().read(sourceUrl);

            return read(source, productKey);
        }
        catch (DocumentException e)
        {
            throw new IllegalArgumentException("Unable to parse permissions from plugin descriptor", e);
        }
    }

    private Set<String> readScopes(Bundle bundle, String productKey) throws IOException
    {
        URL sourceUrl = bundle.getEntry(ATLASSIAN_ADD_ON_JSON);
        String json = getStringFromInputStream(FileUtils.getResource(sourceUrl.toString()));
        ConnectAddonBean addOn = CapabilitiesGsonFactory.getGson().fromJson(json, ConnectAddonBean.class);
        return addOn.getScopes();
    }

    // convert InputStream to String
    private static String getStringFromInputStream(InputStream inputStream) throws IOException
    {

        BufferedReader bufReader = null;
        StringBuilder sb = new StringBuilder();
        String line;

        try
        {
            bufReader = new BufferedReader(new InputStreamReader(inputStream));

            while ((line = bufReader.readLine()) != null)
            {
                sb.append(line);
            }

        }
        finally
        {
            if (bufReader != null)
            {
                try
                {
                    bufReader.close();
                }
                catch (IOException e)
                {
                    log.error("Failed to close BufferedReader. Ignoring!", e);
                }
            }
        }

        return sb.toString();
    }

    private Set<String> read(Document source, String productKey)
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
