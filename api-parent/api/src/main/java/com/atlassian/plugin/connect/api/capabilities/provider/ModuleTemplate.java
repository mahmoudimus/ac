package com.atlassian.plugin.connect.api.capabilities.provider;

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
