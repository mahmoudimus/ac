package com.atlassian.plugin.connect.plugin.capabilities.descriptor.webpanel;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConditionModuleFragmentFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.module.webpanel.IFrameWebPanel;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.spi.web.ProductWebPanelElementEnhancer;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WebPanelConnectModuleDescriptorFactory implements ConnectModuleDescriptorFactory<WebPanelModuleBean,WebPanelModuleDescriptor>
{
    private final ConnectContainerUtil connectContainerUtil;
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;

    @Autowired
    public WebPanelConnectModuleDescriptorFactory(
            ConnectContainerUtil connectContainerUtil,
            ConditionModuleFragmentFactory conditionModuleFragmentFactory)
    {
        this.connectContainerUtil = connectContainerUtil;
        this.conditionModuleFragmentFactory = conditionModuleFragmentFactory;
    }

    @Override
    public WebPanelModuleDescriptor createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, WebPanelModuleBean bean)
    {
        final ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();
        Element domElement = createDomElement(bean, bean.getKey(connectAddonBean), connectAddonBean);
        final WebPanelModuleDescriptor descriptor = connectContainerUtil.createBean(WebPanelConnectModuleDescriptor.class);

        descriptor.init(theConnectPlugin, domElement);
        return descriptor;
    }

    private Element createDomElement(WebPanelModuleBean bean, String webPanelKey, ConnectAddonBean addon)
    {
        String i18nKeyOrName = Strings.isNullOrEmpty(bean.getName().getI18n()) ? bean.getDisplayName() : bean.getName().getI18n();

        Element webPanelElement = new DOMElement("remote-web-panel");
        webPanelElement.addAttribute("key", webPanelKey);
        webPanelElement.addAttribute("i18n-name-key", i18nKeyOrName);
        webPanelElement.addAttribute("location", bean.getLocation());

        if (null != bean.getWeight())
        {
            webPanelElement.addAttribute("weight", Integer.toString(bean.getWeight()));
        }

        if (!bean.getConditions().isEmpty())
        {
            DOMElement conditionFragment = conditionModuleFragmentFactory.createFragment(addon.getKey(), bean.getConditions());
            webPanelElement.add(conditionFragment);
        }

        webPanelElement.addElement("label").addAttribute("key", i18nKeyOrName);
        I18nProperty toolTip = bean.getTooltip();

        if (null != toolTip)
        {
            Element tooltipElement = webPanelElement.addElement("tooltip");
            if (StringUtils.isNotBlank(toolTip.getI18n()))
            {
                tooltipElement.addAttribute("key", toolTip.getI18n());
            }
            tooltipElement.setText(toolTip.getValue());
        }

        webPanelElement.addAttribute("class", IFrameWebPanel.class.getName());
        webPanelElement.addAttribute("width", bean.getLayout().getWidth());
        webPanelElement.addAttribute("height", bean.getLayout().getHeight());
        webPanelElement.addAttribute("url", bean.getUrl());

        for (ProductWebPanelElementEnhancer webPanelElementEnhancer : getProductEnhancers())
        {
            webPanelElementEnhancer.enhance(bean, webPanelElement);
        }

        return webPanelElement;
    }

    private Iterable<ProductWebPanelElementEnhancer> getProductEnhancers()
    {
        return connectContainerUtil.getBeansOfType(ProductWebPanelElementEnhancer.class);
    }
}
