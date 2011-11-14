package com.atlassian.labs.remoteapps.modules.permissions;

import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.modules.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.RemoteModule;
import com.atlassian.labs.remoteapps.modules.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.modules.StartableRemoteModule;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.sal.api.ApplicationProperties;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptySet;

/**
 *
 */
@Component
public class PermissionsModuleGenerator implements RemoteModuleGenerator
{
    private final PermissionManager permissionManager;
    private final String applicationKey;

    @Autowired
    public PermissionsModuleGenerator(PermissionManager permissionManager, ProductAccessor productAccessor)
    {
        this.permissionManager = permissionManager;
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
                    String targetApp = e.attributeValue("application");
                    if (targetApp == null || targetApp.equals(applicationKey))
                    {
                        String scopeKey = e.attributeValue("scope");
                        apiScopes.add(scopeKey);

                    }
                }
                permissionManager.setApiPermissions(ctx.getApplicationType(), apiScopes);
            }
        };
    }
}
