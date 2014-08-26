package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.JiraModuleList;

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
