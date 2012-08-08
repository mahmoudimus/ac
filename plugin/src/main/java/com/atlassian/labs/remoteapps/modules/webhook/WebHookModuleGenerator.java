package com.atlassian.labs.remoteapps.modules.webhook;

import com.atlassian.labs.remoteapps.integration.plugins.SchemaTransformer;
import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.labs.remoteapps.webhook.WebHookRegistrationManager;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredUriAttribute;
import static java.util.Collections.emptyMap;

/**
 * Registers webhooks
 */
@Component
public class WebHookModuleGenerator implements WaitableRemoteModuleGenerator
{
    private final WebHookRegistrationManager webHookRegistrationManager;
    private final Plugin plugin;

    @Autowired
    public WebHookModuleGenerator(WebHookRegistrationManager webHookRegistrationManager,
            PluginRetrievalService pluginRetrievalService)
    {
        this.webHookRegistrationManager = webHookRegistrationManager;
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public String getType()
    {
        return "web-hook";
    }

    @Override
    public String getName()
    {
        return "Web Hook";
    }

    @Override
    public String getDescription()
    {
        return "Registration for a web hook callback from an internal event";
    }

    @Override
    public Schema getSchema()
    {
        return DocumentBasedSchema.builder("webhook")
                .setPlugin(plugin)
                .setTitle(getName())
                .setDescription(getDescription())
                .setTransformer(new SchemaTransformer()
                {
                    @Override
                    public Document transform(Document from)
                    {
                        Element parent = (Element) from.selectSingleNode(
                                "/xs:schema/xs:simpleType/xs:restriction");

                        for (String id : webHookRegistrationManager.getIds())
                        {
                            parent.addElement("xs:enumeration").addAttribute("value", id);
                        }
                        return from;
                    }
                })
                .build();
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

    @Override
    public RemoteModule generate(final RemoteAppCreationContext ctx, Element element)
    {
        return RemoteModule.NO_OP;
    }

    private String getWebHookId(Element element)
    {
        return element.attributeValue("event");
    }

    @Override
    public void validate(Element element, URI registrationUrl, String username) throws PluginParseException
    {
        getRequiredUriAttribute(element, "url");
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
        Element copy = descriptorElement.createCopy("webhook");
        copy.addAttribute("key", "wh-" + copy.attributeValue("event"));
        pluginDescriptorRoot.add(copy);
    }

    @Override
    public void waitToLoad(Element element)
    {
        webHookRegistrationManager.waitForId(getWebHookId(element));
    }
}
