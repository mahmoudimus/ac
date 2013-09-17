package com.atlassian.plugin.connect.plugin.module.confluence.context.serializer;

import com.atlassian.confluence.content.service.PageService;
import com.atlassian.confluence.content.service.page.PageLocator;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.user.User;
import com.atlassian.user.UserManager;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Serializes page objects.
 */
public class PageSerializer extends AbstractConfluenceParameterSerializer<AbstractPage, PageLocator>
{

    public static final String PAGE_FIELD_NAME = "page";

    public PageSerializer(final PageService pageService, UserManager userManager)
    {
        super(userManager, PAGE_FIELD_NAME,
                new ParameterUnwrapper<PageLocator, AbstractPage>()
                {
                    @Override
                    public AbstractPage unwrap(PageLocator wrapped)
                    {
                        return wrapped.getPage();
                    }
                },
                new AbstractConfluenceIdParameterLookup<PageLocator>()
                {
                    @Override
                    public PageLocator lookup(User user, Long id)
                    {
                        // TODO: The confluence page service does not check permissions. Need to do that somewhere
                        return pageService.getIdPageLocator(id);
                    }
                }
        );
    }

    @Override
    public Map<String, Object> serialize(final AbstractPage page)
    {
        return ImmutableMap.<String, Object>of(PAGE_FIELD_NAME, ImmutableMap.of(ID_FIELD_NAME, page.getId()));
    }

    @Override
    protected boolean isResultValid(PageLocator serviceResult)
    {
        return serviceResult.getPage() != null;
    }
}
