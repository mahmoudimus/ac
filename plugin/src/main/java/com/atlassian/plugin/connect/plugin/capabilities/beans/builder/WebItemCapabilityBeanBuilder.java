package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;

import static com.google.common.collect.Lists.newArrayList;

public class WebItemCapabilityBeanBuilder extends NameToKeyBeanBuilder<WebItemCapabilityBeanBuilder, WebItemCapabilityBean>
{
    private String link;
    private String location;
    private AddOnUrlContext context;
    private int weight;
    private List<String> styleClasses;
    private I18nProperty tooltip;
    private IconBean icon;

    public WebItemCapabilityBeanBuilder()
    {

    }

    public WebItemCapabilityBeanBuilder(WebItemCapabilityBean defaultBean)
    {
        super(defaultBean);

        this.link = defaultBean.getLink();
        this.location = defaultBean.getLocation();
        this.context = defaultBean.getContext();
        this.weight = defaultBean.getWeight();
        this.styleClasses = defaultBean.getStyleClasses();
        this.tooltip = defaultBean.getTooltip();
        this.icon = defaultBean.getIcon();
    }

    public WebItemCapabilityBeanBuilder withLink(String link)
    {
        this.link = link;
        return this;
    }

    public WebItemCapabilityBeanBuilder withContext(AddOnUrlContext context)
    {
        this.context = context;
        return this;
    }

    public WebItemCapabilityBeanBuilder withLocation(String location)
    {
        this.location = location;
        return this;
    }

    public WebItemCapabilityBeanBuilder withWeight(int weight)
    {
        this.weight = weight;
        return this;
    }

    public WebItemCapabilityBeanBuilder withStyleClasses(List<String> styleClasses)
    {
        this.styleClasses = styleClasses;
        return this;
    }

    public WebItemCapabilityBeanBuilder withStyleClasses(String ... styleClasses)
    {
        this.styleClasses = newArrayList(styleClasses);
        return this;
    }

    public WebItemCapabilityBeanBuilder withTooltip(I18nProperty tooltip)
    {
        this.tooltip = tooltip;
        return this;
    }

    public WebItemCapabilityBeanBuilder withIcon(IconBean icon)
    {
        this.icon = icon;
        return this;
    }

    @Override
    public WebItemCapabilityBean build()
    {
        return new WebItemCapabilityBean(this);
    }
}
