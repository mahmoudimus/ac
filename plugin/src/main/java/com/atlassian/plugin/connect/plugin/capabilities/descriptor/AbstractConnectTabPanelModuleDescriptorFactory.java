package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AbstractConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.dom.TabPanelElement;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.google.common.base.Optional;
import org.dom4j.Element;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.printNode;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base class for connect TabPanel module descriptor factories
 * @param <B> the type of capability bean
 * @param <D> the type of module descriptor
 */
public class AbstractConnectTabPanelModuleDescriptorFactory<B extends AbstractConnectTabPanelCapabilityBean, D extends ModuleDescriptor>
        implements ConnectModuleDescriptorFactory<B, D>
{
    private static final Logger log = LoggerFactory.getLogger(AbstractConnectTabPanelModuleDescriptorFactory.class);
    private final Class<D> descriptorClass;
    private final ConnectAutowireUtil connectAutowireUtil;
    private final Optional<? extends Class<?>> moduleClass;
    private String domElementName;

    public AbstractConnectTabPanelModuleDescriptorFactory(Class<D> descriptorClass, String domElementName, ConnectAutowireUtil connectAutowireUtil)
    {
        this(descriptorClass, domElementName, connectAutowireUtil, null);
    }

    public AbstractConnectTabPanelModuleDescriptorFactory(Class<D> descriptorClass, String domElementName, ConnectAutowireUtil connectAutowireUtil,
                                                          Class<?> moduleClass)
    {
        this.domElementName = checkNotNull(domElementName);

        this.descriptorClass = checkNotNull(descriptorClass);
        this.connectAutowireUtil = checkNotNull(connectAutowireUtil);
        this.moduleClass = Optional.fromNullable(moduleClass);
    }

    @Override
    public D createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, B bean)
    {
        Element tabPanelElement = new TabPanelElement(domElementName, bean, moduleClass).getElement();

        if (log.isDebugEnabled())
        {
            log.debug("Created tab page: " + printNode(tabPanelElement));
        }

        D descriptor = connectAutowireUtil.createBean(descriptorClass);

        descriptor.init(plugin, tabPanelElement);

        return descriptor;
    }
}
