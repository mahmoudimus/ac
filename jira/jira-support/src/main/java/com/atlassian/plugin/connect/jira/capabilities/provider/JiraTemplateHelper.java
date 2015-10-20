package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.connect.api.capabilities.provider.ModuleTemplate;

public class JiraTemplateHelper
{
    private static final String TEMPLATE_PATH = "velocity/";
    private static final String TEMPLATE_PROJECT_ADMIN_TAB = TEMPLATE_PATH + "iframe-page-project-admin.vm";
    private static final String TEMPLATE_POSTFUNCTION_CREATE = TEMPLATE_PATH + "jira/postfunction/create.vm";
    private static final String TEMPLATE_POSTFUNCTION_EDIT = TEMPLATE_PATH + "jira/postfunction/edit.vm";
    private static final String TEMPLATE_POSTFUNCTION_VIEW = TEMPLATE_PATH + "jira/postfunction/view.vm";

    private static final String TEMPLATE_ACCESS_DENIED_PAGE = TEMPLATE_PATH + "iframe-page-accessdenied.vm";
    private static final String TEMPLATE_ACCESS_DENIED_GENERIC_BODY = TEMPLATE_PATH + "iframe-body-accessdenied.vm";

    private JiraTemplateHelper() {}

    public static ModuleTemplate projectAdminTabTemplate()
    {
        return new ModuleTemplate(TEMPLATE_PROJECT_ADMIN_TAB, TEMPLATE_ACCESS_DENIED_PAGE);
    }

    public static ModuleTemplate workflowPostFunctionTemplate(WorkflowPostFunctionResource resource)
    {
        switch (resource)
        {
            case CREATE: return new ModuleTemplate(TEMPLATE_POSTFUNCTION_CREATE, TEMPLATE_ACCESS_DENIED_GENERIC_BODY);
            case EDIT:   return new ModuleTemplate(TEMPLATE_POSTFUNCTION_EDIT, TEMPLATE_ACCESS_DENIED_GENERIC_BODY);
            case VIEW:   return new ModuleTemplate(TEMPLATE_POSTFUNCTION_VIEW, TEMPLATE_ACCESS_DENIED_GENERIC_BODY);
        }
        throw new RuntimeException("WorkflowPostFunctionResource enum case not covered");
    }
}
