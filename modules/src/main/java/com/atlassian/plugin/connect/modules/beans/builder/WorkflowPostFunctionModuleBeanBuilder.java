package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;


public class WorkflowPostFunctionModuleBeanBuilder extends NameToKeyBeanBuilder<WorkflowPostFunctionModuleBeanBuilder, WorkflowPostFunctionModuleBean>
{

    private I18nProperty description;
    private UrlBean view;
    private UrlBean edit;
    private UrlBean create;
    private UrlBean triggered;

    public WorkflowPostFunctionModuleBeanBuilder()
    {
    }

    public WorkflowPostFunctionModuleBeanBuilder(WorkflowPostFunctionModuleBean defaultBean)
    {
        super(defaultBean);

        this.description = defaultBean.getDescription();
        this.view = defaultBean.getView();
        this.edit = defaultBean.getEdit();
        this.create = defaultBean.getCreate();
        this.triggered = defaultBean.getTriggered();
    }

    public WorkflowPostFunctionModuleBeanBuilder withDescription(I18nProperty description)
    {
        this.description = description;
        return this;
    }

    public WorkflowPostFunctionModuleBeanBuilder withView(UrlBean view)
    {
        this.view = view;
        return this;
    }

    public WorkflowPostFunctionModuleBeanBuilder withEdit(UrlBean edit)
    {
        this.edit = edit;
        return this;
    }

    public WorkflowPostFunctionModuleBeanBuilder withCreate(UrlBean create)
    {
        this.create = create;
        return this;
    }

    public WorkflowPostFunctionModuleBeanBuilder withTriggered(UrlBean triggered)
    {
        this.triggered = triggered;
        return this;
    }

    @Override
    public WorkflowPostFunctionModuleBean build()
    {
        return new WorkflowPostFunctionModuleBean(this);
    }
}
