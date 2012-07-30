package com.atlassian.labs.remoteapps.modules.permissions;

import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import java.util.List;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 7/27/12 Time: 8:33 AM To change this template use
 * File | Settings | File Templates.
 */
public class PermissionsModuleDescriptor extends AbstractModuleDescriptor<Permissions>
{
    private final PermissionManager permissionManager;
    private Permissions permissions;

    public PermissionsModuleDescriptor(PermissionManager permissionManager)
    {
        this.permissionManager = permissionManager;
    }

    @Override
    public void init(@NotNull Plugin plugin, @NotNull Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.permissions = new Permissions(extractApiScopeKeys(element));
    }

    private Set<String> extractApiScopeKeys(Element element)
    {
        Set<String> apiScopes = newHashSet();
        for (Element e : (List<Element>)element.elements("permission"))
        {
            String targetApp = getOptionalAttribute(e, "application", null);
            if (targetApp == null || targetApp.equals(getPluginKey()))
            {
                String scopeKey = getRequiredAttribute(e, "scope");
                apiScopes.add(scopeKey);

            }
        }
        return apiScopes;
    }

    @Override
    public Permissions getModule()
    {
        return permissions;
    }
}
