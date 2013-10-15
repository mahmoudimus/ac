package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.WorkflowPostFunctionCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.UrlBean;


public class WorkflowPostFunctionCapabilityBeanBuilder extends NameToKeyBeanBuilder<WorkflowPostFunctionCapabilityBeanBuilder, WorkflowPostFunctionCapabilityBean>
{
    private I18nProperty description;
    private UrlBean view;
    private UrlBean edit;
    private UrlBean triggered;
    private Boolean allowMultiple;

    public WorkflowPostFunctionCapabilityBeanBuilder()
    {
    }

    public WorkflowPostFunctionCapabilityBeanBuilder(WorkflowPostFunctionCapabilityBean defaultBean)
    {
        super(defaultBean);

        this.description = defaultBean.getDescription();
        this.view = defaultBean.getView();
        this.edit = defaultBean.getEdit();
        this.triggered = defaultBean.getTriggered();
        this.allowMultiple = defaultBean.getAllowMultiple();
    }

    public WorkflowPostFunctionCapabilityBeanBuilder withDescription(I18nProperty description)
    {
        this.description = description;
        return this;
    }

    public WorkflowPostFunctionCapabilityBeanBuilder withView(UrlBean view)
    {
        this.view = view;
        return this;
    }

    public WorkflowPostFunctionCapabilityBeanBuilder withEdit(UrlBean edit)
    {
        this.edit = edit;
        return this;
    }

    public WorkflowPostFunctionCapabilityBeanBuilder withTriggered(UrlBean triggered)
    {
        this.triggered = triggered;
        return this;
    }

    public WorkflowPostFunctionCapabilityBeanBuilder withAllowMultiple(Boolean allowMultiple)
    {
        this.allowMultiple = allowMultiple;
        return this;
    }

    @Override
    public WorkflowPostFunctionCapabilityBean build()
    {
        return new WorkflowPostFunctionCapabilityBean(this);
    }
}
