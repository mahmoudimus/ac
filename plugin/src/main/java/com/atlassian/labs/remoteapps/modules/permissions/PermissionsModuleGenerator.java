package com.atlassian.labs.remoteapps.modules.permissions;

import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.integration.plugins.SchemaTransformer;
import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiResourceInfo;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.labs.remoteapps.settings.SettingsManager;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.sal.api.user.UserManager;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.*;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyMap;

/**
 * Allows a remote app to declare multiple permissions, usually api scopes
 */
@Component
public class PermissionsModuleGenerator implements RemoteModuleGenerator
{
    private final PermissionManager permissionManager;
    private final String applicationKey;
    private final Plugin plugin;

    private final UserManager userManager;
    private final SettingsManager settingsManager;

    @Autowired
    public PermissionsModuleGenerator(PermissionManager permissionManager,
            ProductAccessor productAccessor, UserManager userManager,
            SettingsManager settingsManager, PluginRetrievalService pluginRetrievalService)
    {
        this.permissionManager = permissionManager;
        this.userManager = userManager;
        this.settingsManager = settingsManager;
        this.applicationKey = productAccessor.getKey();
        this.plugin = pluginRetrievalService.getPlugin();
    }


    @Override
    public String getType()
    {
        return "permissions";
    }

    @Override
    public String getName()
    {
        return "Permissions";
    }

    @Override
    public String getDescription()
    {
        return "Defines API scopes for incoming authenticated requests";
    }

    @Override
    public Schema getSchema()
    {
        return DocumentBasedSchema.builder("permissions")
                .setPlugin(plugin)
                .setTitle(getName())
                .setDescription(getDescription())
                .setTransformer(new SchemaTransformer()
                {
                    @Override
                    public Document transform(@Nullable Document input)
                    {
                        Element parent = (Element) input.selectSingleNode("/xs:schema/xs:simpleType/xs:restriction");

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
                        return input;
                    }
                })
                .build();
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

    private List<String> extractApiScopeKeys(Element element)
    {
        List<String> apiScopes = newArrayList();
        for (Element e : (List<Element>)element.elements("permission"))
        {
            String targetApp = getOptionalAttribute(e, "application", null);
            if (targetApp == null || targetApp.equals(applicationKey))
            {
                String scopeKey = getRequiredAttribute(e, "scope");
                apiScopes.add(scopeKey);
            }
        }
        return apiScopes;
    }

    @Override
    public void validate(Element element, URI registrationUrl, String username) throws PluginParseException
    {
        if (!settingsManager.isAllowDogfooding() && !element.elements().isEmpty() && !userManager.isSystemAdmin(username))
        {
            throw new PluginParseException("Cannot install remote app that contains permissions if not either a dogfood server or a system administrator");
        }

        String scopes = StringUtils.join(extractApiScopeKeys(element),",");
        // this number comes from the limitation in sal property settings that cannot store more
        // 255 characters in the setting's value
        if (scopes.length() > 220)
        {
            throw new PluginParseException("Cannot install remote app that contains too many " +
                    "permissions.");
        }
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
        Element perms = pluginDescriptorRoot.element("plugin-info").addElement("permissions");
        for (String scope : extractApiScopeKeys(descriptorElement))
        {
            perms.addElement("permission").addText(scope);
        }
    }
}
