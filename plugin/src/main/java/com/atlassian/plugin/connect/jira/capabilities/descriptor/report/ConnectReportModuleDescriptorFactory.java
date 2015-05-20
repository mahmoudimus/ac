package com.atlassian.plugin.connect.jira.capabilities.descriptor.report;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.api.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.api.iframe.servlet.ConnectIFrameServletHelper;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ReportModuleBean;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Creates Connect implementation of ReportModuleDescriptor.
 */
@JiraComponent
public class ConnectReportModuleDescriptorFactory implements ConnectModuleDescriptorFactory<ReportModuleBean, ConnectReportModuleDescriptor>
{
    private final ConnectContainerUtil containerUtil;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final AbsoluteAddOnUrlConverter absoluteAddOnUrlConverter;

    @Autowired
    public ConnectReportModuleDescriptorFactory(final ConnectContainerUtil containerUtil,
            final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            final AbsoluteAddOnUrlConverter absoluteAddOnUrlConverter)
    {
        this.containerUtil = containerUtil;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.absoluteAddOnUrlConverter = absoluteAddOnUrlConverter;
    }

    @Override
    public ConnectReportModuleDescriptor createModuleDescriptor(final ConnectModuleProviderContext moduleProviderContext,
            final Plugin plugin, final ReportModuleBean bean)
    {
        ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();
        Element reportModule = createReportDescriptor(bean, connectAddonBean);

        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addOn(connectAddonBean.getKey())
                .module(bean.getKey(connectAddonBean))
                .pageTemplate()
                .urlTemplate(bean.getUrl())
                .decorator("atl.general")
                .resizeToParent(true)
                .title(bean.getDisplayName())
                .build();

        iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), bean.getRawKey(), renderStrategy);

        ConnectReportModuleDescriptor moduleDescriptor = containerUtil.createBean(ConnectReportModuleDescriptor.class);
        moduleDescriptor.setThumbnailUrl(getThumbnailUrl(moduleProviderContext.getConnectAddonBean(), bean.getThumbnailUrl()));
        moduleDescriptor.init(plugin, reportModule);
        return moduleDescriptor;
    }

    private Element createReportDescriptor(final ReportModuleBean bean, final ConnectAddonBean connectAddonBean)
    {
        final String iFrameServletPath = ConnectIFrameServletHelper.iFrameServletPath(connectAddonBean.getKey(), bean.getRawKey());

        final Element reportModule = new DOMElement("report");
        reportModule.addAttribute("key", bean.getKey(connectAddonBean));
        reportModule.addAttribute("i18n-name-key", bean.getName().getI18n());
        reportModule.addAttribute("class", ConnectReport.class.getName());
        reportModule.addAttribute("url", iFrameServletPath);
        reportModule.addAttribute("name", bean.getName().getValue());

        reportModule.addElement("description")
                .addAttribute("key", bean.getDescription().getI18n())
                .setText(bean.getDescription().getValue());

        reportModule.addElement("label")
                .addAttribute("key", bean.getName().getI18n())
                .setText(bean.getName().getValue());

        reportModule.addElement("category")
                .addAttribute("key", bean.getReportCategory().getKey());

        reportModule.addElement("thumbnail")
                .addAttribute("cssClass", ConnectReportModuleDescriptor.getThumbnailCssClass(bean.getKey(connectAddonBean)));

        return reportModule;
    }

    private String getThumbnailUrl(ConnectAddonBean connectAddonBean, String thumbnailUrl)
    {
        return StringUtils.isEmpty(thumbnailUrl) ? "" : absoluteAddOnUrlConverter.getAbsoluteUrl(connectAddonBean, thumbnailUrl);
    }
}
