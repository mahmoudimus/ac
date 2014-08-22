package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ReportModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.report.ConnectReport;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.report.ConnectReportModuleDescriptor;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.provider.GeneralPageModuleProvider;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.iframe.servlet.ConnectIFrameServlet;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
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
    private final String productBaseUrl;

    @Autowired
    public ConnectReportModuleDescriptorFactory(final ConnectContainerUtil containerUtil,
            final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            ApplicationProperties applicationProperties)
    {
        this.containerUtil = containerUtil;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.productBaseUrl = applicationProperties.getBaseUrl(UrlMode.RELATIVE_CANONICAL);
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
                .decorator(GeneralPageModuleProvider.ATL_GENERAL_DECORATOR)
                .resizeToParent(true)
                .title(bean.getDisplayName())
                .build();

        iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), bean.getRawKey(), renderStrategy);

        ConnectReportModuleDescriptor moduleDescriptor = containerUtil.createBean(ConnectReportModuleDescriptor.class);
        moduleDescriptor.init(plugin, reportModule);
        return moduleDescriptor;
    }

    private Element createReportDescriptor(final ReportModuleBean bean, final ConnectAddonBean connectAddonBean)
    {
        final String iFrameServletPath = ConnectIFrameServlet.iFrameServletPath(connectAddonBean.getKey(), bean.getRawKey());

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

        return reportModule;
    }
}
