package com.atlassian.labs.remoteapps.modules.permissions;

import com.atlassian.labs.remoteapps.api.XmlUtils;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.labs.remoteapps.util.BundleUtil;
import com.atlassian.plugin.Plugin;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
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

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getOptionalAttribute;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Reads permissions from a plugin instance.  Currently it extracts the permissions from the descriptor
 * by loading the document, but soon will read natively from Plugin
 */
@Component
public class PermissionsReader
{
    private final Cache<Plugin,Permissions> permissionsCache;
    private final BundleContext bundleContext;

    @Autowired
    public PermissionsReader(final ProductAccessor productAccessor, BundleContext bundleContext)
    {
        this.bundleContext = bundleContext;
        this.permissionsCache = CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<Plugin,Permissions>()
        {
            @Override
            public Permissions load(Plugin plugin) throws Exception
            {
                return read(plugin, productAccessor);
            }
        });
    }

    public Permissions getPermissionsForPlugin(Plugin plugin)
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

    private Permissions read(Plugin plugin, ProductAccessor productAccessor)
    {
        try
        {
            Bundle bundle = BundleUtil.findBundleForPlugin(bundleContext, plugin.getKey());
            URL sourceUrl = bundle.getEntry("atlassian-plugin.xml");
            Document source = XmlUtils.createSecureSaxReader().read(sourceUrl);

            Set<String> list = newHashSet();
            Element permissionsElement = source.getRootElement().element("plugin-info").element(
                    "permissions");
            if (permissionsElement != null)
            {

                for (Element e : (List<Element>)permissionsElement.elements())
                {
                    String application = getOptionalAttribute(e, "application", productAccessor.getKey());
                    if (productAccessor.getKey().equals(application))
                    {
                        list.add(e.getTextTrim());
                    }
                }
            }

            return new Permissions(list);
        }
        catch (DocumentException e)
        {
            throw new IllegalArgumentException("Unable to parse permissions from plugin descriptor", e);
        }
    }
}
