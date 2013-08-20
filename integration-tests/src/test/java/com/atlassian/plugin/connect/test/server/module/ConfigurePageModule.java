package com.atlassian.plugin.connect.test.server.module;

import java.net.URI;

import org.dom4j.Element;

public final class ConfigurePageModule extends MainModuleWithResource<ConfigurePageModule>
{
    private ConfigurePageModule(String key)
    {
        super("configure-page", key);
    }

    public static ConfigurePageModule key(String key)
    {
        return new ConfigurePageModule(key);
    }

    @Override
    protected void addYetOthersToElement(Element el)
    {
        // bah...
        final Element root = el.getDocument().getRootElement();
        root.element("plugin-info")
            .addElement("param")
            .addAttribute("name", "configure.url")
            .addText("/plugins/servlet" + createLocalUrl(root.attributeValue("key"), path.getOrNull()));
    }

    public static String createLocalUrl(String pluginKey, String pageUrl)
    {
        return URI.create("/atlassian-connect/" + pluginKey + (pageUrl.startsWith("/") ? "" : "/") + pageUrl).toString();
    }
}
