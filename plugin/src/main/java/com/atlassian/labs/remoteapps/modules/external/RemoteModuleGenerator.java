package com.atlassian.labs.remoteapps.modules.external;

import com.atlassian.plugin.PluginParseException;
import org.dom4j.Element;

import java.util.Map;
import java.util.Set;

/**
 * A remote module generator.  To provide a new remote module generator, an instance must be
 * exposed as a public OSGi service.  It is done this way instead of a dynamic module type as
 * all implementations are needed at creation time.  For example, if there is a remote app installed
 * and the remote app plugin has been upgraded, the remote app wouldn't be able to start as the
 * dynamic module impls aren't available yet as the remote app plugin isn't "enabled".  OSGi services,
 * on the other hand, are available before the plugin is considered enabled.
 */
public interface RemoteModuleGenerator extends SchemaDocumented
{
    String getType();

    Schema getSchema();

    Map<String,String> getI18nMessages(String pluginKey, Element element);

    RemoteModule generate(RemoteAppCreationContext ctx, Element element);

    void validate(Element element, String registrationUrl, String username) throws PluginParseException;

    void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot);
}
