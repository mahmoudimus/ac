package com.atlassian.plugin.connect.jira.capabilities.provider;

public class ModuleTemplate
{
    public final String template;
    public final String deniedAccessTemplate;

    public ModuleTemplate(final String templateProjectAdminTab, final String templateAccessDeniedPage)
    {
        this.template = templateProjectAdminTab;
        this.deniedAccessTemplate = templateAccessDeniedPage;
    }
}
