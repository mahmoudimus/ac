package com.atlassian.plugin.connect.plugin.module.confluence.context.extractor;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.plugin.connect.plugin.module.confluence.context.serializer.PageSerializer;
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
 * Extracts page parameters that can be included in webpanel's iframe url.
 */
public class PageContextMapParameterExtractor implements ContextMapParameterExtractor<AbstractPage>
{
    private static final String PAGE_CONTEXT_PARAMETER = "page";
    private final PageSerializer pageSerializer;
    private final PermissionManager permissionManager;
    private final UserManager userManager;
    private static final Logger LOGGER = LoggerFactory.getLogger(PageContextMapParameterExtractor.class);

    public PageContextMapParameterExtractor(PageSerializer pageSerializer, PermissionManager permissionManager, UserManager userManager)
    {
        this.pageSerializer = pageSerializer;
        this.permissionManager = checkNotNull(permissionManager, "permissionManager is mandatory");
        this.userManager = checkNotNull(userManager, "userManager is mandatory");
    }


    @Override
    public Optional<AbstractPage> extract(final Map<String, Object> context)
    {
        if (context.containsKey("webInterfaceContext"))
        {
            WebInterfaceContext webInterfaceContext = (WebInterfaceContext) context.get("webInterfaceContext");
            if (null != webInterfaceContext && null != webInterfaceContext.getPage())
            {
                return Optional.of(webInterfaceContext.getPage());

            }
        }
        else if (context.containsKey(PAGE_CONTEXT_PARAMETER) && context.get(PAGE_CONTEXT_PARAMETER) instanceof AbstractPage)
        {
            return Optional.of((AbstractPage) context.get(PAGE_CONTEXT_PARAMETER));
        }
        return Optional.absent();
    }

    @Override
    public ParameterSerializer<AbstractPage> serializer()
    {
        return pageSerializer;
    }

    @Override
    public boolean hasViewPermission(String username, AbstractPage page)
    {
        try
        {
            return permissionManager.hasPermission(userManager.getUser(username), VIEW, page);
        }
        catch (EntityException e)
        {
            LOGGER.error("Failed to check permission. Defaulting to denying access", e);
            return false;
        }
    }
}
