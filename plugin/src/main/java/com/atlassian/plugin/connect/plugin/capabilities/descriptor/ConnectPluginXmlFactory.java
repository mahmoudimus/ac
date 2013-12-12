package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import javax.annotation.Nullable;
import java.util.List;

import com.atlassian.fugue.Effect;
import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConfigurePageModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ModuleList;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.RelativeAddOnUrlConverter;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.stereotype.Component;

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

        // populate the addon's configure url if we have exactly one configure module marked as default
        Option<String> defaultConfigureUrl = findDefaultConfigureModuleUrl(bean);
        defaultConfigureUrl.foreach(new Effect<String>()
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

    private Option<String> findDefaultConfigureModuleUrl(final ConnectAddonBean bean)
    {
        Option<String> defaultConfigureModuleKey = findDefaultConfigureModuleKey(bean.getModules());

        return defaultConfigureModuleKey
                .map(new Function<String, String>()
                {
                    @Override
                    public String apply(@Nullable String moduleKey)
                    {
                        return relativeAddOnUrlConverter.addOnUrlToLocalServletUrl(bean.getKey(),
                                moduleKey).getRelativeUri();
                    }
                });

    }

    private Option<String> findDefaultConfigureModuleKey(ModuleList modules)
    {
        List<ConfigurePageModuleBean> configurePages = modules.getConfigurePages();

        int numConfigurePages = configurePages.size();
        if (numConfigurePages == 0)
        {
            return Option.none();
        }
        if (numConfigurePages == 1)
        {
            // default to the only configure page
            return Option.some(configurePages.get(0).getKey());
        }

        // we have more than one configurePage. There must be exactly one marked as default

        Iterable<ConfigurePageModuleBean> defaultPages = Iterables.filter(configurePages,
                new Predicate<ConfigurePageModuleBean>()
                {
                    @Override
                    public boolean apply(@Nullable ConfigurePageModuleBean configurePage)
                    {
                        return configurePage.isDefault();
                    }
                });

        int numDefaultConfigurePages = Iterables.size(defaultPages);


        switch (numDefaultConfigurePages)
        {
            case 0:
                throw new InvalidAddonConfigurationException("More than one configPage specified but none has their " +
                        "default field set to true. Must have exactly one set to true");
            case 1:
                return Option.some(Iterables.getFirst(defaultPages, null).getKey());
            default:
                throw new InvalidAddonConfigurationException("Can only have one configPage marked as default");
        }

    }
}
