package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.atlassian.plugin.connect.plugin.module.webpanel.IFrameRemoteWebPanel;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.google.common.base.Strings;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class WebPanelConnectModuleDescriptorFactory implements ConnectModuleDescriptorFactory<WebPanelModuleBean,WebPanelModuleDescriptor>
{
    public static final String REMOTE_WEB_PANEL_MODULE_PREFIX = "remote-web-panel-";
    private final ConnectAutowireUtil connectAutowireUtil;
    private final ConditionModuleFragmentFactory conditionModuleFragmentFactory;

    @Autowired
    public WebPanelConnectModuleDescriptorFactory(ConnectAutowireUtil connectAutowireUtil, ConditionModuleFragmentFactory conditionModuleFragmentFactory)
    {
        this.connectAutowireUtil = connectAutowireUtil;
        this.conditionModuleFragmentFactory = conditionModuleFragmentFactory;
    }

    @Override
    public WebPanelModuleDescriptor createModuleDescriptor(Plugin plugin, WebPanelModuleBean bean)
    {
        Element domElement = createDomElement(bean, REMOTE_WEB_PANEL_MODULE_PREFIX + bean.getKey(), plugin);
        final WebPanelModuleDescriptor descriptor = connectAutowireUtil.createBean(WebPanelConnectModuleDescriptor.class);
        descriptor.init(plugin, domElement);
        return descriptor;
    }

    private Element createDomElement(WebPanelModuleBean bean, String webPanelKey, Plugin plugin)
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
            DOMElement conditionFragment = conditionModuleFragmentFactory.createFragment(plugin.getKey(), bean.getConditions(), "#" + webPanelKey);
            webPanelElement.add(conditionFragment);
        }

        webPanelElement.addElement("label").addAttribute("key", i18nKeyOrName);
        webPanelElement.addAttribute("class", IFrameRemoteWebPanel.class.getName());
        webPanelElement.addAttribute("width", bean.getLayout().getWidth());
        webPanelElement.addAttribute("height", bean.getLayout().getHeight());
        webPanelElement.addAttribute("url", bean.getUrl());

        return webPanelElement;
    }
}
