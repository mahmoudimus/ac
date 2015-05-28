package com.atlassian.plugin.connect.core.capabilities.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebSectionModuleDescriptor;
import org.dom4j.Element;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConnectWebSectionModuleDescriptor extends DefaultWebSectionModuleDescriptor
{
    public ConnectWebSectionModuleDescriptor(final WebInterfaceManager webInterfaceManager)
    {
        super(webInterfaceManager);
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        checkNotNull(element.attributeValue("key"));
        super.init(plugin, element);
    }

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }
}
