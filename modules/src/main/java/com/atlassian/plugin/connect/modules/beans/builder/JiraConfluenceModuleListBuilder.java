package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.JiraConfluenceModuleList;

public class JiraConfluenceModuleListBuilder extends JiraModuleListBuilder
{

    @Override
    protected JiraConfluenceModuleList createEmpty()
    {
        return new JiraConfluenceModuleList();
    }

    @Override
    public JiraConfluenceModuleList build()
    {
        return modules;
    }
}
