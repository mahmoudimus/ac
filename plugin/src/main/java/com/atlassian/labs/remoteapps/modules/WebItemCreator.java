package com.atlassian.labs.remoteapps.modules;

import com.atlassian.labs.remoteapps.modules.external.RemoteAppCreationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import org.dom4j.Element;

import java.util.Map;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;

public class WebItemCreator
{
    private final WebItemContext webItemContext;

    public WebItemCreator(WebItemContext webItemContext)
    {
        this.webItemContext = webItemContext;
    }

    public ModuleDescriptor createWebItemDescriptor(RemoteAppCreationContext ctx,
                                                     Element e,
                                                     String key,
                                                     String localUrl
    )
    {
        Element config = e.createCopy();
        final String webItemKey = "webitem-" + key;
        config.addAttribute("key", webItemKey);
        config.addAttribute("section",
                getOptionalAttribute(e, "section", webItemContext.getPreferredSectionKey()));
        config.addAttribute("weight", getOptionalAttribute(e, "weight", webItemContext.getPreferredWeight()));

        if (localUrl.contains("$"))
        {
            throw new PluginParseException("Invalid url '" + localUrl + "', cannot contain velocity expressions");
        }

        StringBuilder url = new StringBuilder();
        url.append("/plugins/servlet");
        url.append(localUrl);
        if (!localUrl.contains("?"))
        {
            url.append("?");
        }

        for (Map.Entry<String,String> entry : webItemContext.getContextParams().entrySet())
        {
            url.append(entry.getKey());
            url.append("=");
            url.append(entry.getValue());
        }
        String name = getRequiredAttribute(e, "name");
        config.addElement("label").setText(name);
        config.addElement("link").
                addAttribute("linkId", webItemKey).
                setText(url.toString());

        ModuleDescriptor descriptor = ctx.getAccessLevel()
                                         .createWebItemModuleDescriptor(ctx.getBundle().getBundleContext());
        descriptor.init(ctx.getPlugin(), config);
        return descriptor;
    }
}
