package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import javax.annotation.Nullable;

import com.atlassian.fugue.Effect;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.RelativeAddOnUrlConverter;
import com.google.common.base.Function;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.stereotype.Component;

import static com.atlassian.fugue.Option.option;

@Component
public class ConnectPluginXmlFactory
{
    private final static RelativeAddOnUrlConverter relativeAddOnUrlConverter = new RelativeAddOnUrlConverter();

    public String createPluginXml(ConnectAddonBean bean)
    {
        Document doc = DocumentFactory.getInstance().createDocument();
        Element root = new DOMElement("atlassian-plugin");
        doc.setRootElement(root);

        root.addAttribute("key", bean.getKey())
                .addAttribute("name", bean.getName())
                .addAttribute("plugins-version", "2");

        final Element info = new DOMElement("plugin-info");
        info.addElement("description").setText(bean.getDescription());
        info.addElement("version").setText(bean.getVersion());
        info.addElement("vendor")
                .addAttribute("name", bean.getVendor().getName())
                .addAttribute("url", bean.getVendor().getUrl());

        if (null != bean.getEnableLicensing() && bean.getEnableLicensing())
        {
            info.addElement("param").addAttribute("name", "atlassian-licensing-enabled").setText("true");
        }

        // populate the addon's configure url if we a configure configure module
        findConfigureModuleUrl(bean).foreach(new Effect<String>()
        {
            @Override
            public void apply(String url)
            {
                info.addElement("param").addAttribute("name", "configure.url").setText(url);
            }
        });

        root.add(info);

        return doc.asXML();

    }

    private Option<String> findConfigureModuleUrl(final ConnectAddonBean addon)
    {
        return option(addon.getModules().getConfigurePage()).map(new Function<ConnectPageModuleBean, String>()
        {
            @Override
            public String apply(@Nullable ConnectPageModuleBean pageModule)
            {
                return relativeAddOnUrlConverter.addOnUrlToLocalServletUrl(addon.getKey(), pageModule.getUrl()).getRelativeUri();
            }
        });
    }
}
