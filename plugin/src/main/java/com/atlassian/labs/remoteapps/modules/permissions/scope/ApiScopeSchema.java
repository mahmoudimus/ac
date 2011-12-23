package com.atlassian.labs.remoteapps.modules.permissions.scope;

import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.descriptor.external.ApiScopeModuleDescriptor;
import com.atlassian.labs.remoteapps.descriptor.external.DynamicSchema;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.dom4j.Document;
import org.dom4j.Element;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.addSchemaDocumentation;

/**
 *
 */
public class ApiScopeSchema extends DynamicSchema
{
    private final PermissionManager permissionManager;
    protected ApiScopeSchema(PluginRetrievalService pluginRetrievalService, PermissionManager permissionManager)
    {
        super(pluginRetrievalService.getPlugin(), "permissions.xsd", "/xsd/permissions.xsd", "PermissionsType", "1");
        this.permissionManager = permissionManager;
    }

    @Override
    protected Document transform(Document from)
    {
        Element parent = (Element) from.selectSingleNode("/xs:schema/xs:simpleType/xs:restriction");

        for (ApiScopeModuleDescriptor descriptor : permissionManager.getApiScopeDescriptors())
        {
            Element enumeration = parent.addElement("xs:enumeration").addAttribute("value", descriptor.getKey());
            Element doc = addSchemaDocumentation(enumeration, descriptor);
            Element resources = doc.addElement("resources");
            for (ApiResourceInfo resource : descriptor.getModule().getApiResourceInfos())
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
