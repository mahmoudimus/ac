package com.atlassian.labs.remoteapps.plugin.module.webhook;

import com.atlassian.labs.remoteapps.spi.schema.SchemaTransformer;
import com.atlassian.labs.remoteapps.plugin.webhook.WebHookRegistrationManager;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Registers a web hook module descriptor factory
 */
public class WebHookSchemaFactory implements SchemaTransformer
{
    private final WebHookRegistrationManager webHookRegistrationManager;

    public WebHookSchemaFactory(WebHookRegistrationManager webHookRegistrationManager)
    {
        this.webHookRegistrationManager = webHookRegistrationManager;

    }

    @Override
    public Document transform(Document document)
    {
        Element parent = (Element) document.selectSingleNode(
                "/xs:schema/xs:simpleType/xs:restriction");

        for (String id : webHookRegistrationManager.getIds())
        {
            parent.addElement("xs:enumeration").addAttribute("value", id);
        }
        return document;
    }
}
