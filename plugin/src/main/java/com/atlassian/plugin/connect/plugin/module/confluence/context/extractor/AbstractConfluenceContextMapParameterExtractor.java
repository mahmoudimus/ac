package com.atlassian.plugin.connect.plugin.module.confluence.context.extractor;

import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.plugin.connect.plugin.module.context.AbstractContextMapParameterExtractor;
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
 * Base class for Confluence related ContextMapParameterExtractors
 *
 * @param P the parameter (resource) type
 */
public abstract class AbstractConfluenceContextMapParameterExtractor<P> extends AbstractContextMapParameterExtractor<P>
{
    private static final String WEB_INTERFACE_CONTEXT_KEY = "webInterfaceContext";
    private final PermissionManager permissionManager;
    private final UserManager userManager;
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConfluenceContextMapParameterExtractor.class);

    public AbstractConfluenceContextMapParameterExtractor(Class<P> resourceClass, ParameterSerializer<P> parameterSerializer,
                                                          String contextParameterKey, PermissionManager permissionManager,
                                                          UserManager userManager)
    {
        super(resourceClass, parameterSerializer, contextParameterKey);
        this.permissionManager = checkNotNull(permissionManager, "permissionManager is mandatory");
        this.userManager = checkNotNull(userManager, "userManager is mandatory");
    }


    @Override
    public Optional<P> extract(final Map<String, Object> context)
    {
        if (context.containsKey(WEB_INTERFACE_CONTEXT_KEY))
        {
            WebInterfaceContext webInterfaceContext = (WebInterfaceContext) context.get(WEB_INTERFACE_CONTEXT_KEY);
            if (null != webInterfaceContext)
            {
                final P resource = getResource(webInterfaceContext);
                if (null != resource)
                {
                    return Optional.of(resource);
                }
            }
        }
        else
        {
            return super.extract(context);
        }

        return Optional.absent();
    }


    protected abstract P getResource(WebInterfaceContext webInterfaceContext);

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
