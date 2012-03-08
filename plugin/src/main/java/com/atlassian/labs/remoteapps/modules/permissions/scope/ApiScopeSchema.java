package com.atlassian.labs.remoteapps.modules.permissions.scope;

import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.modules.external.DynamicSchema;
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
public class ApiScopeSchema extends DynamicSchema
{
    private final PermissionManager permissionManager;

    @Autowired
    public ApiScopeSchema(PluginRetrievalService pluginRetrievalService, PermissionManager permissionManager)
    {
        super(pluginRetrievalService.getPlugin(), "permissions.xsd", "/xsd/permissions.xsd", "PermissionsType", "1");
        this.permissionManager = permissionManager;
    }

    @Override
    protected Document transform(Document from)
    {
        Element parent = (Element) from.selectSingleNode("/xs:schema/xs:simpleType/xs:restriction");

        for (ApiScope apiScope : permissionManager.getApiScopes())
        {
            Element enumeration = parent.addElement("xs:enumeration").addAttribute("value", apiScope.getKey());
            Element doc = addSchemaDocumentation(enumeration, apiScope);
            Element resources = doc.addElement("resources");
            for (ApiResourceInfo resource : apiScope.getApiResourceInfos())
            {

                Element res = resources.addElement("resource").
                    addAttribute("path", resource.getPath()).
                    addAttribute("httpMethod", resource.getHttpMethod());
                if (resource.getRpcMethod() != null)
                {
                    res.addAttribute("rpcMethod", resource.getRpcMethod());
                }
            }
        }
        return from;
    }
}
