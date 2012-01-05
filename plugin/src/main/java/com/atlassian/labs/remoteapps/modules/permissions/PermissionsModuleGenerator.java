package com.atlassian.labs.remoteapps.modules.permissions;

import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.modules.external.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.external.RemoteModule;
import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.modules.external.StartableRemoteModule;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.labs.remoteapps.settings.SettingsManager;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

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
public class PermissionsModuleGenerator implements RemoteModuleGenerator
{
    private final PermissionManager permissionManager;
    private final String applicationKey;

    private final UserManager userManager;
    private final SettingsManager settingsManager;

    @Autowired
    public PermissionsModuleGenerator(PermissionManager permissionManager, ProductAccessor productAccessor, UserManager userManager, SettingsManager settingsManager)
    {
        this.permissionManager = permissionManager;
        this.userManager = userManager;
        this.settingsManager = settingsManager;
        this.applicationKey = productAccessor.getKey();
    }


    @Override
    public String getType()
    {
        return "permissions";
    }

    @Override
    public Set<String> getDynamicModuleTypeDependencies()
    {
        return emptySet();
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
                permissionManager.setApiPermissions(ctx.getApplicationType(), apiScopes);
            }
        };
    }

    @Override
    public void validate(Element element, String registrationUrl, String username) throws PluginParseException
    {
        if (!settingsManager.isAllowDogfooding() && !element.elements().isEmpty() && !userManager.isAdmin(username))
        {
            throw new PluginParseException("Cannot install remote app that contains permissions if not either a dogfood server or an administrator");
        }
    }

    @Override
    public void convertDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
    }
}
