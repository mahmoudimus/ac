package com.atlassian.labs.remoteapps.descriptor.external;

import com.atlassian.labs.remoteapps.loader.RemoteAppLoader;
import com.atlassian.labs.remoteapps.util.BundleUtil;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.LegacyModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The module descriptor for remote-app
 * @deprecated
 */
public class RemoteAppModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final BundleContext bundleContext;
    private final RemoteAppLoader remoteAppLoader;
    private volatile boolean enabled;
    private static final Logger log = LoggerFactory.getLogger(RemoteAppModuleDescriptor.class);

    private Element originalElement;

    public RemoteAppModuleDescriptor(BundleContext bundleContext,
            RemoteAppLoader remoteAppLoader)
    {
        super(new LegacyModuleFactory());
        this.bundleContext = bundleContext;
        this.remoteAppLoader = remoteAppLoader;
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.originalElement = element;
    }

    @Override
    public void enabled()
    {
        log.debug("Enabling remote app {}", getPluginKey());
        super.enabled();
        if (!enabled)
        {
            enabled = true;
            Document appDoc = DocumentHelper.createDocument(originalElement.createCopy());
            try
            {
                remoteAppLoader.load(BundleUtil.findBundleForPlugin(bundleContext, getPluginKey()),
                        appDoc);
            }
            catch (Exception e)
            {
                throw new PluginParseException("Unable to load remote app", e);
            }
        }
    }

    @Override
    public void disabled()
    {
        log.debug("Disabling '{}'", getPluginKey());
        super.disabled();
        enabled = false;
    }

    @Override
    public Void getModule()
    {
        return null;
    }
}
