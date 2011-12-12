package com.atlassian.labs.remoteapps.modules.permissions.scope;

import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.descriptor.external.ApiScopeModuleDescriptor;
import com.atlassian.labs.remoteapps.descriptor.external.DynamicSchema;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.dom4j.Document;
import org.dom4j.Element;

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
            parent.addElement("xs:enumeration").addAttribute("value", descriptor.getKey());
        }
        return from;
    }
}
