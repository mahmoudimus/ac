package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;

import static com.atlassian.plugin.connect.plugin.module.util.redirect.RedirectServlet.getPermanentRedirectUrl;

@Component
public class IconModuleFragmentFactory implements ConnectModuleFragmentFactory<IconBean>
{
    private final RemotablePluginAccessorFactory pluginAccessorFactory;

    @Autowired
    public IconModuleFragmentFactory(RemotablePluginAccessorFactory pluginAccessorFactory)
    {
        this.pluginAccessorFactory = pluginAccessorFactory;
    }

    @Override
    public DOMElement createFragment(String pluginKey, IconBean bean)
    {
        String pluginBaseUrl = pluginAccessorFactory.get(pluginKey).getBaseUrl().toString();

        URI iconUri = URI.create(pluginBaseUrl + bean.getUrl());

        DOMElement element = new DOMElement("icon");
        element.addAttribute("width", Integer.toString(bean.getWidth()))
              .addAttribute("height", Integer.toString(bean.getHeight()))
              .addElement("link")
              .addText(getPermanentRedirectUrl(pluginKey, iconUri));
        
        return element;
    }
}
