package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.plugin.connect.plugin.scopes.AddOnScope;
import com.atlassian.plugin.connect.plugin.scopes.StaticAddOnScopes;
import com.atlassian.sal.api.ApplicationProperties;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class ScopeServiceImpl implements ScopeService
{
    private final ApplicationProperties applicationProperties;

    @Autowired
    public ScopeServiceImpl(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = checkNotNull(applicationProperties);
    }

    @Override
    public Collection<AddOnScope> build() throws IOException
    {
        String applicationDisplayName = applicationProperties.getDisplayName();

        if (StringUtils.isEmpty(applicationDisplayName))
        {
            throw new IllegalArgumentException("Application display name can be neither null nor blank");
        }

        String lowerCaseDisplayName = applicationDisplayName.toLowerCase();

        // alternately we could send the display name straight through to StaticAddOnScopes.buildFor(String)
        // but with a name like "display name" I'm not confident that it won't contain formatting or extra characters
        if (lowerCaseDisplayName.contains("confluence"))
        {
            return StaticAddOnScopes.buildForConfluence();
        }

        if (lowerCaseDisplayName.contains("jira"))
        {
            return StaticAddOnScopes.buildForJira();
        }

        throw new IllegalArgumentException(String.format("Application display name '%s' is not recognised as either Confluence or JIRA. Please set it to a value that when converted to lower case contains either 'confluence' or 'jira'.", applicationDisplayName));
    }
}
