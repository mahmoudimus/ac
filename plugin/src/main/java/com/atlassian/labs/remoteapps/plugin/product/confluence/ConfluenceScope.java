package com.atlassian.labs.remoteapps.plugin.product.confluence;

import com.atlassian.labs.remoteapps.api.service.confluence.*;
import com.atlassian.labs.remoteapps.spi.permission.scope.*;
import com.atlassian.labs.remoteapps.spi.util.RequirePermission;
import com.atlassian.velocity.htmlsafe.util.Check;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.apache.commons.lang.ClassUtils;
import org.codehaus.jackson.map.util.ClassUtil;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.unmodifiableIterable;

/**
 *
 */
abstract class ConfluenceScope implements ApiScope, MutablePermission
{
    private final XmlRpcApiScopeHelper v2XmlRpcApiScopeHelper;
    private final XmlRpcApiScopeHelper v1XmlRpcApiScopeHelper;
    private final JsonRpcApiScopeHelper v2JsonRpcScopeHelper;
    private final JsonRpcApiScopeHelper v1JsonRpcScopeHelper;
    private RestApiScopeHelper restApiScopeHelper;
    private final Iterable<ApiResourceInfo> apiResourceInfo;

    private static final Map<String,Collection<String>> serviceClassMethodsByPermission;
    private final String permissionName;

    private String name;
    private String description;

    static
    {
        serviceClassMethodsByPermission = Collections.unmodifiableMap(
                scanServiceClasses(ConfluencePageClient.class, ConfluenceAdminClient.class, ConfluenceAttachmentClient.class,
                        ConfluenceLabelClient.class, ConfluenceNotificationClient.class, ConfluenceSpaceClient.class,
                        ConfluenceUserClient.class, ConfluenceBlogClient.class
                ));
    }

    private static Map<String, Collection<String>> scanServiceClasses(Class<?>... serviceInterfaces)
    {
        Multimap<String,String> result = HashMultimap.create();
        for (Class serviceClass : serviceInterfaces)
        {
            for (Method method : serviceClass.getMethods())
            {
                RequirePermission permission = method.getAnnotation(RequirePermission.class);
                if (permission != null)
                {
                    result.put(permission.value(), method.getName());
                }
            }
        }
        return result.asMap();
    }

    protected ConfluenceScope(String permissionName)
    {
        this(permissionName, Collections.<RestApiScopeHelper.RestScope>emptyList());
    }

    protected ConfluenceScope(String permissionName, Collection<RestApiScopeHelper.RestScope> resources)
    {
        this.permissionName = permissionName;
        Collection<String> methods = serviceClassMethodsByPermission.get(permissionName);
        Check.notNull(methods);

        v1JsonRpcScopeHelper = new JsonRpcApiScopeHelper("/rpc/json-rpc/confluenceservice-v1", methods);
        v2JsonRpcScopeHelper = new JsonRpcApiScopeHelper("/rpc/json-rpc/confluenceservice-v2", methods);
        v1XmlRpcApiScopeHelper = new XmlRpcApiScopeHelper("/rpc/xmlrpc", Collections2.transform(methods, xmlRpcTransform("confluence1")));
        v2XmlRpcApiScopeHelper = new XmlRpcApiScopeHelper("/rpc/xmlrpc", Collections2.transform(methods, xmlRpcTransform("confluence2")));
        restApiScopeHelper = new RestApiScopeHelper(resources);
        this.apiResourceInfo = concat(v1JsonRpcScopeHelper.getApiResourceInfos(), v2JsonRpcScopeHelper.getApiResourceInfos(), v1XmlRpcApiScopeHelper.getApiResourceInfos(), v2XmlRpcApiScopeHelper.getApiResourceInfos());
    }

    @Override
    public String getKey()
    {
        return permissionName;
    }

    @Override
    public boolean allow(HttpServletRequest request, String user)
    {
        return v1XmlRpcApiScopeHelper.allow(request, user) || v2XmlRpcApiScopeHelper.allow(request, user) || v1JsonRpcScopeHelper.allow(request, user) || v2JsonRpcScopeHelper.allow(request, user) || restApiScopeHelper.allow(request, user);
    }

    @Override
    public Iterable<ApiResourceInfo> getApiResourceInfos()
    {
        return apiResourceInfo;
    }

    private Function<String, String> xmlRpcTransform(final String serviceName)
    {
        return new Function<String, String>()
        {

            @Override
            public String apply(String from)
            {
                return serviceName + "." + from;
            }
        };
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }
}
