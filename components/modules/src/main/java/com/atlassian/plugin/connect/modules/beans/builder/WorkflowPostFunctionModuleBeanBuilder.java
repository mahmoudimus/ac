package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;


public class WorkflowPostFunctionModuleBeanBuilder extends RequiredKeyBeanBuilder<WorkflowPostFunctionModuleBeanBuilder, WorkflowPostFunctionModuleBean>
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
        this.view = checkNotAbsolute(view);
        return this;
    }

    public WorkflowPostFunctionModuleBeanBuilder withEdit(UrlBean edit)
    {
        this.edit = checkNotAbsolute(edit);
        return this;
    }

    public WorkflowPostFunctionModuleBeanBuilder withCreate(UrlBean create)
    {
        this.create = checkNotAbsolute(create);
        return this;
    }

    public WorkflowPostFunctionModuleBeanBuilder withTriggered(UrlBean triggered)
    {
        this.triggered = checkNotAbsolute(triggered);
        return this;
    }

    // don't send workflow details to arbitrary external urls
    private UrlBean checkNotAbsolute(UrlBean urlBean)
    {
        if (null != urlBean)
        {
            final String url = urlBean.getUrl();

            if (null != url && url.toLowerCase().startsWith("http"))
            {
               throw new IllegalArgumentException(String.format("Workflow post-function URLs must not be absolute: [%s]", url));
            }
        }

        return urlBean;
    }

    @Override
    public WorkflowPostFunctionModuleBean build()
    {
        return new WorkflowPostFunctionModuleBean(this);
    }
}
