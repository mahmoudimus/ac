package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.IssueTabPageCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;

public class IssueTabPageCapabilityBeanBuilder extends NameToKeyBeanBuilder<IssueTabPageCapabilityBeanBuilder, IssueTabPageCapabilityBean>
{
    private String url;
    private int weight;
    private IconBean icon;

    public IssueTabPageCapabilityBeanBuilder()
    {

    }

    public IssueTabPageCapabilityBeanBuilder(IssueTabPageCapabilityBean defaultBean)
    {
        super(defaultBean);

        this.url = defaultBean.getUrl();
        this.weight = defaultBean.getWeight();
        this.icon = defaultBean.getIcon();
    }

    public IssueTabPageCapabilityBeanBuilder withUrl(String link)
    {
        this.url = link;
        return this;
    }

    public IssueTabPageCapabilityBeanBuilder withWeight(int weight)
    {
        this.weight = weight;
        return this;
    }

    public IssueTabPageCapabilityBeanBuilder withIcon(IconBean icon)
    {
        this.icon = icon;
        return this;
    }

    @Override
    public IssueTabPageCapabilityBean build()
    {
        return new IssueTabPageCapabilityBean(this);
    }
}
