package com.atlassian.plugin.connect.jira.report;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.request.AbsoluteAddonUrlConverter;
import com.atlassian.plugin.connect.api.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.api.web.iframe.ConnectIFrameServletPath;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ReportModuleBean;
import com.atlassian.plugin.connect.api.lifecycle.ConnectModuleDescriptorFactory;
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
    private final AbsoluteAddonUrlConverter absoluteAddonUrlConverter;

    @Autowired
    public ConnectReportModuleDescriptorFactory(final ConnectContainerUtil containerUtil,
            final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            final AbsoluteAddonUrlConverter absoluteAddonUrlConverter)
    {
        this.containerUtil = containerUtil;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.absoluteAddonUrlConverter = absoluteAddonUrlConverter;
    }

    @Override
    public ConnectReportModuleDescriptor createModuleDescriptor(final ReportModuleBean bean, ConnectAddonBean connectAddonBean, final Plugin plugin)
    {
        Element reportModule = createReportDescriptor(bean, connectAddonBean);

        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addon(connectAddonBean.getKey())
                .module(bean.getKey(connectAddonBean))
                .pageTemplate()
                .urlTemplate(bean.getUrl())
                .decorator("atl.general")
                .resizeToParent(true)
                .title(bean.getDisplayName())
                .build();

        iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), bean.getRawKey(), renderStrategy);

        ConnectReportModuleDescriptor moduleDescriptor = containerUtil.createBean(ConnectReportModuleDescriptor.class);
        moduleDescriptor.setThumbnailUrl(getThumbnailUrl(connectAddonBean, bean.getThumbnailUrl()));
        moduleDescriptor.init(plugin, reportModule);
        return moduleDescriptor;
    }

    private Element createReportDescriptor(final ReportModuleBean bean, final ConnectAddonBean connectAddonBean)
    {
        final String iFrameServletPath = ConnectIFrameServletPath.forModule(connectAddonBean.getKey(), bean.getRawKey());

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
        return StringUtils.isEmpty(thumbnailUrl) ? "" : absoluteAddonUrlConverter.getAbsoluteUrl(connectAddonBean, thumbnailUrl);
    }
}
