package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@ConfluenceComponent
public class PageIdContextParameter implements PageContextParameterMapper.PageParameter
{

    private static final String PARAMETER_KEY = "page.id";

    private ConfluenceContextParameterViewPermissionManager viewPermissionManager;
    private PageManager pageManager;

    @Autowired
    public PageIdContextParameter(ConfluenceContextParameterViewPermissionManager viewPermissionManager,
            PageManager pageManager)
    {
        this.viewPermissionManager = viewPermissionManager;
        this.pageManager = pageManager;
    }

    @Override
    public boolean isAccessibleByCurrentUser(AbstractPage contextValue)
    {
        return viewPermissionManager.isParameterValueAccessibleByCurrentUser(contextValue);
    }

    @Override
    public boolean isValueAccessibleByCurrentUser(String value)
    {
        return Optional.ofNullable(pageManager.getAbstractPage(Long.valueOf(value)))
                .map(this::isAccessibleByCurrentUser).orElse(false);
    }

    @Override
    public String getKey()
    {
        return PARAMETER_KEY;
    }

    @Override
    public String getValue(AbstractPage contextValue)
    {
        return Long.toString(contextValue.getId());
    }
}
