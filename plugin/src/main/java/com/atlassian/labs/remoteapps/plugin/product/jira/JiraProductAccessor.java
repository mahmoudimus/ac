package com.atlassian.labs.remoteapps.plugin.product.jira;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebItemModuleDescriptor;
import com.atlassian.labs.remoteapps.plugin.product.ProductAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 *
 */
public class JiraProductAccessor implements ProductAccessor
{
    private final WebInterfaceManager webInterfaceManager;

    public JiraProductAccessor(WebInterfaceManager webInterfaceManager)
    {
        this.webInterfaceManager = webInterfaceManager;
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor()
    {
        return new JiraWebItemModuleDescriptor(ComponentManager.getInstance().getJiraAuthenticationContext(), webInterfaceManager);
    }

    @Override
    public String getPreferredAdminSectionKey()
    {
        return "system.admin/system";
    }

    @Override
    public int getPreferredAdminWeight()
    {
        return 150;
    }

    @Override
    public String getKey()
    {
        return "jira";
    }

    @Override
    public int getPreferredGeneralWeight()
    {
        return 100;
    }

    @Override
    public String getPreferredGeneralSectionKey()
    {
        return "general_dropdown_linkId/jira-remoteapps.general";
    }

    @Override
    public int getPreferredProfileWeight()
    {
        return 100;
    }

    @Override
    public String getPreferredProfileSectionKey()
    {
        return "system.user.options/personal";
    }

    @Override
    public Map<String, String> getLinkContextParams()
    {
        return ImmutableMap.of(
                "project_id", "$!helper.project.id",
                "issue_id", "$!issue.id");
    }
}
