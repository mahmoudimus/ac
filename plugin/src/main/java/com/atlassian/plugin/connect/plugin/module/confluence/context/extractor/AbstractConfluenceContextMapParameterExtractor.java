package com.atlassian.plugin.connect.plugin.module.confluence.context.extractor;

import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.plugin.connect.plugin.module.context.ContextMapParameterExtractor;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.atlassian.user.EntityException;
import com.atlassian.user.UserManager;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.atlassian.confluence.security.Permission.VIEW;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Extracts resource parameters that can be included in webpanel's iframe url.
 */
public abstract class AbstractConfluenceContextMapParameterExtractor<P> implements ContextMapParameterExtractor<P>
{
    private final Class<P> resourceClass;
    private final ParameterSerializer<P> parameterSerializer;
    private final String contextParameter;
    private final PermissionManager permissionManager;
    private final UserManager userManager;
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConfluenceContextMapParameterExtractor.class);

    public AbstractConfluenceContextMapParameterExtractor(Class<P> resourceClass, ParameterSerializer<P> parameterSerializer, String contextParameter,
                                                          PermissionManager permissionManager, UserManager userManager)
    {
        this.resourceClass = resourceClass;
        this.parameterSerializer = parameterSerializer;
        this.contextParameter = contextParameter;
        this.permissionManager = checkNotNull(permissionManager, "permissionManager is mandatory");
        this.userManager = checkNotNull(userManager, "userManager is mandatory");
    }


    @Override
    public Optional<P> extract(final Map<String, Object> context)
    {
        if (context.containsKey("webInterfaceContext"))
        {
            WebInterfaceContext webInterfaceContext = (WebInterfaceContext) context.get("webInterfaceContext");
            if (null != webInterfaceContext && null != webInterfaceContext.getPage())
            {
                return Optional.of(getResource(webInterfaceContext));

            }
        }
        else if (context.containsKey(contextParameter) && resourceClass.isInstance(context.get(contextParameter)))
        {
            return Optional.of((P) context.get(contextParameter));
        }
        return Optional.absent();
    }

    protected abstract P getResource(WebInterfaceContext webInterfaceContext);

    @Override
    public ParameterSerializer<P> serializer()
    {
        return parameterSerializer;
    }

    @Override
    public boolean hasViewPermission(String username, P resource)
    {
        try
        {
            return permissionManager.hasPermission(userManager.getUser(username), VIEW, resource);
        }
        catch (EntityException e)
        {
            LOGGER.error("Failed to check permission. Defaulting to denying access", e);
            return false;
        }
    }
}
