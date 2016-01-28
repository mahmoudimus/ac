package com.atlassian.plugin.connect.jira.customfield.type;

import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyService;
import com.atlassian.jira.entity.property.EntityPropertyService.PropertyInput;
import com.atlassian.jira.entity.property.EntityPropertyService.SetPropertyValidationResult;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import org.springframework.stereotype.Component;

@Component
public class JsonPropertyStoreImpl implements JsonPropertyStore
{
    private final IssuePropertyService issuePropertyService;
    private final JiraAuthenticationContext authContext;

    public JsonPropertyStoreImpl(final IssuePropertyService issuePropertyService, final JiraAuthenticationContext authContext)
    {
        this.issuePropertyService = issuePropertyService;
        this.authContext = authContext;
    }

    @Override
    public void storeValue(final String fieldTypeId, final String fieldId, final Issue issue, final String value)
    {
        ApplicationUser currentUser = authContext.getLoggedInUser();

        PropertyInput property = new PropertyInput(value, propertyKey(fieldTypeId, fieldId));
        SetPropertyValidationResult setPropertyValidationResult = issuePropertyService.validateSetProperty(currentUser, issue.getId(), property);
        if (setPropertyValidationResult.isValid())
        {
            issuePropertyService.setProperty(currentUser, setPropertyValidationResult);
        }
        else
        {
            throw new IllegalStateException("This should not have happened");
        }
    }

    @Override
    public String loadValue(final String fieldTypeId, final String fieldId, final Issue issue)
    {
        EntityPropertyService.PropertyResult property = issuePropertyService.getProperty(authContext.getLoggedInUser(), issue.getId(), propertyKey(fieldTypeId, fieldId));

        return property.getEntityProperty().map(EntityProperty::getValue).getOrNull();
    }

    private String propertyKey(String fieldTypeId, String fieldId) {
        return fieldTypeId + ":" + fieldId;
    }
}
