package com.atlassian.plugin.connect.plugin.web;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import javax.servlet.Filter;

public final class ConnectRequestFilterModuleDescriptorImpl extends AbstractModuleDescriptor<Filter> implements ConnectRequestFilterModuleDescriptor
{
    private final ResettableLazyReference<Filter> moduleLazyReference = new ResettableLazyReference<Filter>()
    {
        @Override
        protected Filter create() throws Exception
        {
            return moduleFactory.createModule(moduleClassName, ConnectRequestFilterModuleDescriptorImpl.this);
        }
    };
    private ConnectRequestFilterPhase filterPhase;

    public ConnectRequestFilterModuleDescriptorImpl(final ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    public Filter getModule()
    {
        return moduleLazyReference.get();
    }

    @Override
    public void init(@NotNull final Plugin plugin, @NotNull final Element element) throws PluginParseException
    {
        super.init(plugin, element);

        final String phase = element.attributeValue("phase");
        if (StringUtils.isEmpty(phase))
        {
            throw new PluginParseException("Connect request filter phase must not be empty");
        }

        this.filterPhase = ConnectRequestFilterPhase.getConnectRequestFilterPhase(phase).fold(new Supplier<ConnectRequestFilterPhase>()
        {
            @Override
            public ConnectRequestFilterPhase get()
            {
                throw new PluginParseException("Invalid Connect request filter phase. " + filterPhase + " is not supported.");
            }
        }, new Function<ConnectRequestFilterPhase, ConnectRequestFilterPhase>()
        {
            @Override
            public ConnectRequestFilterPhase apply(final ConnectRequestFilterPhase filterPhase)
            {
                return filterPhase;
            }
        });
    }

    @Override
    public void disabled()
    {
        moduleLazyReference.reset();
        super.disabled();
    }

    public ConnectRequestFilterPhase getFilterPhase()
    {
        return filterPhase;
    }
}
