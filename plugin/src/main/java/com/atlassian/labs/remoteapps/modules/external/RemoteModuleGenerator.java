package com.atlassian.labs.remoteapps.modules.external;

import com.atlassian.plugin.PluginParseException;
import org.dom4j.Element;

import java.util.Map;
import java.util.Set;

/**
 * A remote module generator
 */
public interface RemoteModuleGenerator
{
    String getType();

    Set<String> getDynamicModuleTypeDependencies();

    Map<String,String> getI18nMessages(String pluginKey, Element element);

    RemoteModule generate(RemoteAppCreationContext ctx, Element element);

    void validate(Element element) throws PluginParseException;

    void convertDescriptor(Element descriptorElement, Element pluginDescriptorRoot);
}
