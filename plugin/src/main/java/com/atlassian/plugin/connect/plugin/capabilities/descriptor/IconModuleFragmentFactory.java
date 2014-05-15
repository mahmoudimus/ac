package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.modules.beans.nested.IconBean;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;

import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class IconModuleFragmentFactory
{
    private final RemotablePluginAccessorFactory pluginAccessorFactory;

    @Autowired
    public IconModuleFragmentFactory(RemotablePluginAccessorFactory pluginAccessorFactory)
    {
        this.pluginAccessorFactory = pluginAccessorFactory;
    }

    public DOMElement createFragment(String pluginKey, IconBean bean)
    {
        URI url;
        URI iconUri = URI.create(bean.getUrl());
        
        //if the url is absolute, just use it
        if(iconUri.isAbsolute())
        {
            url = iconUri;
        }
        else
        {
            url = pluginAccessorFactory.get(pluginKey).getTargetUrl(iconUri);   
        }
        
        DOMElement element = new DOMElement("icon");
        element.addAttribute("width", Integer.toString(bean.getWidth()))
              .addAttribute("height", Integer.toString(bean.getHeight()))
              .addElement("link")
              .addText(url.toString());

        return element;
    }
}
