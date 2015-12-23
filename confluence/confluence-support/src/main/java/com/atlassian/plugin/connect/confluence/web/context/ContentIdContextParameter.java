package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.core.ContentEntityManager;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Optional;

@ConfluenceComponent
public class ContentIdContextParameter implements ContentContextParameterMapper.ContentParameter
{

    private static final String PARAMETER_KEY = "content.id";

    private ConfluenceContextParameterViewPermissionManager viewPermissionManager;
    private ContentEntityManager contentEntityManager;

    @Autowired
    public ContentIdContextParameter(ConfluenceContextParameterViewPermissionManager viewPermissionManager,
            @Qualifier("contentEntityManager") ContentEntityManager contentEntityManager)
    {
        this.viewPermissionManager = viewPermissionManager;
        this.contentEntityManager = contentEntityManager;
    }

    @Override
    public boolean isAccessibleByCurrentUser(ContentEntityObject contextValue)
    {
        return viewPermissionManager.isParameterValueAccessibleByCurrentUser(contextValue);
    }

    @Override
    public boolean isValueAccessibleByCurrentUser(String value)
    {
        return Optional.ofNullable(contentEntityManager.getById(Long.valueOf(value)))
                .map(this::isAccessibleByCurrentUser).orElse(false);
    }

    @Override
    public String getKey()
    {
        return PARAMETER_KEY;
    }

    @Override
    public String getValue(ContentEntityObject contextValue)
    {
        return Long.toString(contextValue.getId());
    }
}
