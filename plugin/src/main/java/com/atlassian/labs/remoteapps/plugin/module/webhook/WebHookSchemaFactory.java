package com.atlassian.labs.remoteapps.plugin.module.webhook;

import com.atlassian.labs.remoteapps.plugin.webhook.WebHookIdsAccessor;
import com.atlassian.labs.remoteapps.spi.schema.SchemaTransformer;
import org.dom4j.Document;
import org.dom4j.Element;

import static com.google.common.base.Preconditions.*;

/**
 * Registers a web hook module descriptor factory
 */
public final class WebHookSchemaFactory implements SchemaTransformer
{
    private final WebHookIdsAccessor webHookIdsAccessor;

    public WebHookSchemaFactory(WebHookIdsAccessor webHookIdsAccessor)
    {
        this.webHookIdsAccessor = checkNotNull(webHookIdsAccessor);
    }

    @Override
    public Document transform(Document document)
    {
        final Element parent = (Element) document.selectSingleNode("/xs:schema/xs:simpleType/xs:restriction");
        for (String id : webHookIdsAccessor.getIds())
        {
            parent.addElement("xs:enumeration").addAttribute("value", id);
        }
        return document;
    }
}
