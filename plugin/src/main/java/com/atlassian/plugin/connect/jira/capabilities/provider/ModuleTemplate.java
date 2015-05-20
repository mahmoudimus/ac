package com.atlassian.plugin.connect.jira.capabilities.provider;

public class ModuleTemplate
{
    public final String template;
    public final String accessDeniedTemplate;

    public ModuleTemplate(final String template, final String accessDeniedTemplate)
    {
        this.template = template;
        this.accessDeniedTemplate = accessDeniedTemplate;
    }
}
