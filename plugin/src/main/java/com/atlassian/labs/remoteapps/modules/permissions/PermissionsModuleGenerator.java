package com.atlassian.labs.remoteapps.modules.permissions;

import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScopeSchema;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.labs.remoteapps.settings.SettingsManager;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.sal.api.user.UserManager;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

/**
 * Allows a remote app to declare multiple permissions, usually api scopes
 */
@Component
public class PermissionsModuleGenerator implements WaitableRemoteModuleGenerator
{
    private final PermissionManager permissionManager;
    private final String applicationKey;

    private final UserManager userManager;
    private final SettingsManager settingsManager;
    private final ApiScopeSchema apiScopeSchema;

    @Autowired
    public PermissionsModuleGenerator(PermissionManager permissionManager,
            ProductAccessor productAccessor, UserManager userManager,
            SettingsManager settingsManager, ApiScopeSchema apiScopeSchema)
    {
        this.permissionManager = permissionManager;
        this.userManager = userManager;
        this.settingsManager = settingsManager;
        this.apiScopeSchema = apiScopeSchema;
        this.applicationKey = productAccessor.getKey();
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
        return apiScopeSchema;
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

    @Override
    public RemoteModule generate(final RemoteAppCreationContext ctx, final Element element)
    {
        return new StartableRemoteModule()
        {
            @Override
            public Set<ModuleDescriptor> getModuleDescriptors()
            {
                return emptySet();
            }

            @Override
            public void start()
            {
                List<String> apiScopes = extractApiScopeKeys(element);
                permissionManager.setApiPermissions(ctx.getApplicationType(), apiScopes);
            }
        };
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
    public void validate(Element element, String registrationUrl, String username) throws PluginParseException
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
    }

    @Override
    public void waitToLoad(Element moduleElement)
    {
        permissionManager.waitForApiScopes(extractApiScopeKeys(moduleElement));
    }
}
