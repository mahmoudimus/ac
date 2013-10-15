package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class WebPanelCapabilityBeanBuilder extends NameToKeyBeanBuilder<WebPanelCapabilityBeanBuilder, WebPanelCapabilityBean>
{
    private String link;
    private String location;
    private AddOnUrlContext context;
    private int weight;
    private List<String> styleClasses;
    private I18nProperty tooltip;
    private IconBean icon;

    public WebPanelCapabilityBeanBuilder()
    {

    }

    public WebPanelCapabilityBeanBuilder(WebPanelCapabilityBean defaultBean)
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

    public WebPanelCapabilityBeanBuilder withLink(String link)
    {
        this.link = link;
        return this;
    }

    public WebPanelCapabilityBeanBuilder withContext(AddOnUrlContext context)
    {
        this.context = context;
        return this;
    }

    public WebPanelCapabilityBeanBuilder withLocation(String location)
    {
        this.location = location;
        return this;
    }

    public WebPanelCapabilityBeanBuilder withWeight(int weight)
    {
        this.weight = weight;
        return this;
    }

    public WebPanelCapabilityBeanBuilder withStyleClasses(List<String> styleClasses)
    {
        this.styleClasses = styleClasses;
        return this;
    }

    public WebPanelCapabilityBeanBuilder withStyleClasses(String ... styleClasses)
    {
        this.styleClasses = newArrayList(styleClasses);
        return this;
    }

    public WebPanelCapabilityBeanBuilder withTooltip(I18nProperty tooltip)
    {
        this.tooltip = tooltip;
        return this;
    }

    public WebPanelCapabilityBeanBuilder withIcon(IconBean icon)
    {
        this.icon = icon;
        return this;
    }

    @Override
    public WebPanelCapabilityBean build()
    {
        return new WebPanelCapabilityBean(this);
    }
}
