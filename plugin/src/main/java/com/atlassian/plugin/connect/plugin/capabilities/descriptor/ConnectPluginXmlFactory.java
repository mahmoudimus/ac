package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.stereotype.Component;

@Component
public class ConnectPluginXmlFactory
{
    public String createPluginXml(ConnectAddonBean bean)
    {
        Document doc = DocumentFactory.getInstance().createDocument();
        Element root = new DOMElement("atlassian-plugin");
        doc.setRootElement(root);

        root.addAttribute("key",bean.getKey())
                .addAttribute("name", bean.getName())
                .addAttribute("plugins-version","2");
        
        Element info = new DOMElement("plugin-info");
        info.addElement("description").setText(bean.getDescription());
        info.addElement("version").setText(bean.getVersion());
        info.addElement("vendor")
            .addAttribute("name",bean.getVendor().getName())
            .addAttribute("url",bean.getVendor().getUrl());
        
        root.add(info);

        return doc.asXML();
                
    }
}
