package com.atlassian.plugin.connect.modules.beans.builder.jira;

import com.atlassian.plugin.connect.modules.beans.builder.ModuleListBuilder;
import com.atlassian.plugin.connect.modules.beans.jira.JiraModuleList;

public abstract class JiraModuleListBuilder extends ModuleListBuilder<JiraModuleListBuilder, JiraModuleList>
{

    @Override
    protected JiraModuleList createEmpty()
    {
        return new JiraModuleList();
    }

    @Override
    public JiraModuleList build()
    {
        return modules;
    }

}
