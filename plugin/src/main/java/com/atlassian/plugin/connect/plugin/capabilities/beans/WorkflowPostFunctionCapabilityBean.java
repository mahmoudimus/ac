package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.WorkflowPostFunctionCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.UrlBean;

public class WorkflowPostFunctionCapabilityBean extends NameToKeyBean
{
    private I18nProperty description;
    private UrlBean view;
    private UrlBean edit;
    private UrlBean create;
    private UrlBean triggered;

    public WorkflowPostFunctionCapabilityBean()
    {
        this.description = new I18nProperty("", "");
        this.view = null;
        this.edit = null;
        this.create = null;
        this.triggered = null;
    }

    public WorkflowPostFunctionCapabilityBean(WorkflowPostFunctionCapabilityBeanBuilder builder)
    {
        super(builder);

        if (null == description)
        {
            this.description = new I18nProperty("", "");
        }
    }

    public I18nProperty getDescription()
    {
        return description;
    }

    public UrlBean getView()
    {
        return view;
    }

    public boolean hasView()
    {
        return null != view;
    }

    public UrlBean getEdit()
    {
        return edit;
    }

    public boolean hasEdit()
    {
        return null != edit;
    }

    public UrlBean getCreate()
    {
        return create;
    }

    public boolean hasCreate()
    {
        return null != create;
    }

    public UrlBean getTriggered()
    {
        return triggered;
    }

    public boolean hasTriggered()
    {
        return null != triggered;
    }

    public static WorkflowPostFunctionCapabilityBeanBuilder newWorkflowPostFunctionBean()
    {
        return new WorkflowPostFunctionCapabilityBeanBuilder();
    }

    public static WorkflowPostFunctionCapabilityBeanBuilder newWorkflowPostFunctionBean(WorkflowPostFunctionCapabilityBean defaultBean)
    {
        return new WorkflowPostFunctionCapabilityBeanBuilder(defaultBean);
    }
}
