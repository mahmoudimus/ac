package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.JiraConfluenceModuleList;

public class JiraConfluenceModuleListBuilder extends
        ModuleListBuilder<JiraConfluenceModuleListBuilder, JiraConfluenceModuleList>
{

    @Override
    protected JiraConfluenceModuleList createEmpty()
    {
        return new JiraConfluenceModuleList();
    }

    @Override
    public JiraConfluenceModuleList build()
    {
        return new JiraConfluenceModuleList(this);
    }
}
