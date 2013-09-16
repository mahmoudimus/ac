package com.atlassian.plugin.connect.plugin.module.confluence.context.serializer;

import com.atlassian.confluence.content.service.PageService;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.plugin.connect.plugin.module.context.ParameterDeserializer;
import com.atlassian.plugin.connect.plugin.module.context.ParameterSerializer;
import com.atlassian.user.UserManager;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Serializes page objects.
 */
public class PageSerializer implements ParameterSerializer<AbstractPage>, ParameterDeserializer<AbstractPage>
{
    private final PageService pageService;
    private final UserManager userManager;

    public PageSerializer(PageService pageService, UserManager userManager)
    {

        this.pageService = checkNotNull(pageService, "pageService is mandatory");
        this.userManager = checkNotNull(userManager, "userManager is mandatory");
    }

    @Override
    public Map<String, Object> serialize(final AbstractPage page)
    {
        return ImmutableMap.<String, Object>of("page",
                ImmutableMap.of("id", page.getId())
        );
    }

    @Override
    public Optional<AbstractPage> deserialize(Map<String, Object> params, String username)
    {
        return null;
    }
}
