package com.atlassian.labs.remoteapps.modules.webhook;

import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.modules.external.DynamicSchema;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiResourceInfo;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.webhook.WebHookRegistrationManager;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.addSchemaDocumentation;

/**
 *
 */
@Component
public class WebHookSchema extends DynamicSchema
{
    private final WebHookRegistrationManager webHookRegistrationManager;

    @Autowired
    public WebHookSchema(PluginRetrievalService pluginRetrievalService,
            WebHookRegistrationManager webHookRegistrationManager)
    {
        super(pluginRetrievalService.getPlugin(), "webhook.xsd", "/xsd/webhook.xsd", "WebHookType",
                "unbounded");
        this.webHookRegistrationManager = webHookRegistrationManager;
    }

    @Override
    protected Document transform(Document from)
    {
        Element parent = (Element) from.selectSingleNode("/xs:schema/xs:simpleType/xs:restriction");

        for (String id : webHookRegistrationManager.getIds())
        {
            parent.addElement("xs:enumeration").addAttribute("value", id);
        }
        return from;
    }
}
