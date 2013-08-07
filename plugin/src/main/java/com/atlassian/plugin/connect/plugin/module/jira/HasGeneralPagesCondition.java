package com.atlassian.plugin.connect.plugin.module.jira;

import java.util.Map;

import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.product.jira.JiraProductAccessor;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.WebInterfaceManager;

public class HasGeneralPagesCondition implements Condition
{
    private final WebInterfaceManager webInterfaceManager;
    private final JiraProductAccessor jiraProductAccessor;

    public HasGeneralPagesCondition(WebInterfaceManager webInterfaceManager,
            JiraProductAccessor jiraProductAccessor)
    {
        this.webInterfaceManager = webInterfaceManager;
        this.jiraProductAccessor = jiraProductAccessor;
    }


    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context)
    {
        return !webInterfaceManager.getDisplayableItems(
                jiraProductAccessor.getPreferredGeneralSectionKey(), context).isEmpty();
    }
}
