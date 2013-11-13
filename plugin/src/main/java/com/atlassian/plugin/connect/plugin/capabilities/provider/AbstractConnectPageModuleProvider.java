package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.PageToWebItemAndServletConverter;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IFramePageServletDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.web.Condition;
import com.google.common.collect.ImmutableList;
import org.osgi.framework.BundleContext;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base class for ConnectModuleProviders of Connect Pages.
 * Note that there is actually no P2 module descriptor. Instead it is modelled as a web-item plus a servlet
 */
public abstract class AbstractConnectPageModuleProvider implements ConnectModuleProvider<ConnectPageCapabilityBean>
{
    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    private final IFramePageServletDescriptorFactory servletDescriptorFactory;
    private final ProductAccessor productAccessor;
    private String decorator;
    private String templateSuffix;
    private Map<String, String> metaTagContents;
    private Condition condition;

    public AbstractConnectPageModuleProvider(WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                             IFramePageServletDescriptorFactory servletDescriptorFactory,
                                             ProductAccessor productAccessor,
                                             String decorator, String templateSuffix,
                                             Map<String, String> metaTagContents, Condition condition)
    {
        this.webItemModuleDescriptorFactory = checkNotNull(webItemModuleDescriptorFactory);
        this.servletDescriptorFactory = checkNotNull(servletDescriptorFactory);
        this.productAccessor = checkNotNull(productAccessor);
        this.decorator = decorator;
        this.templateSuffix = templateSuffix;
        this.metaTagContents = metaTagContents;
        this.condition = condition;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, String jsonFieldName,
                                                 List<ConnectPageCapabilityBean> beans)
    {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();

        for (ConnectPageCapabilityBean bean : beans)
        {
            PageToWebItemAndServletConverter converter = new PageToWebItemAndServletConverter(bean, plugin.getKey(),
                    productAccessor, decorator, templateSuffix, metaTagContents, condition);
            builder.add(webItemModuleDescriptorFactory.createModuleDescriptor(plugin, addonBundleContext, converter.getWebItemBean()));
            builder.add(servletDescriptorFactory.createIFrameServletDescriptor(plugin, converter.getServletBean()));
        }

        return builder.build();
    }
}
