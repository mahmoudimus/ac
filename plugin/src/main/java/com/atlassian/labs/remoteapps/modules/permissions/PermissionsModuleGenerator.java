package com.atlassian.labs.remoteapps.modules.permissions;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.labs.remoteapps.ApiPermissionManager;
import com.atlassian.labs.remoteapps.modules.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.RemoteModule;
import com.atlassian.labs.remoteapps.modules.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.modules.StartableRemoteModule;
import com.atlassian.plugin.ModuleDescriptor;
import org.dom4j.Element;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptySet;

/**
 *
 */
public class PermissionsModuleGenerator implements RemoteModuleGenerator
{
    private final ApiPermissionManager apiPermissionManager;

    public PermissionsModuleGenerator(ApiPermissionManager apiPermissionManager)
    {
        this.apiPermissionManager = apiPermissionManager;
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
                List<String> readApis = newArrayList();
                List<String> writeApis = newArrayList();
                for (Element e : (List<Element>)element.elements("permission"))
                {
                    String api = e.attributeValue("rest-api");
                    boolean writable = Boolean.parseBoolean(e.attributeValue("write"));
                    readApis.add(api);
                    if (writable)
                    {
                        writeApis.add(api);
                    }
                }
                apiPermissionManager.setPermissions(ctx.getApplicationType(), readApis, writeApis);
            }
        };
    }
}
