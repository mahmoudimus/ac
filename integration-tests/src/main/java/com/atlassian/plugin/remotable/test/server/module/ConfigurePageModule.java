package com.atlassian.plugin.remotable.test.server.module;

import com.atlassian.fugue.Option;
import org.dom4j.Element;

import java.net.URI;

import static com.atlassian.fugue.Option.option;
import static com.atlassian.fugue.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;

public final class ConfigurePageModule extends AbstractModuleWithResource<ConfigurePageModule>
{
    private final String key;
    private Option<String> name;

    private ConfigurePageModule(String key)
    {
        super("configure-page");
        this.key = checkNotNull(key);
    }

    public static ConfigurePageModule key(String key)
    {
        return new ConfigurePageModule(key);
    }

    public ConfigurePageModule name(String name)
    {
        this.name = option(name);
        return this;
    }

    @Override
    protected void addToElement(Element el)
    {
        addAttribute(el, "key", some(key));
        addAttribute(el, "name", name);
        addAttribute(el, "url", path);

        // bah...
        final Element root = el.getDocument().getRootElement();
        root.element("plugin-info")
                .addElement("param")
                .addAttribute("name", "configure.url")
                .addText("/plugins/servlet" + createLocalUrl(root.attributeValue("key"), path.getOrNull()));
    }

    public static String createLocalUrl(String pluginKey, String pageUrl)
    {
        return URI.create("/remotable-plugins/" + pluginKey + (pageUrl.startsWith("/") ? "" : "/") + pageUrl).toString();
    }
}
