package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemTargetBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class WebItemModuleBeanBuilder extends BeanWithKeyParamsAndConditionsBuilder<WebItemModuleBeanBuilder, WebItemModuleBean>
{
    private String link;
    private String location;
    private AddOnUrlContext context;
    private Integer weight;
    private List<String> styleClasses;
    private I18nProperty tooltip;
    private IconBean icon;
    private WebItemTargetBean target;

    public WebItemModuleBeanBuilder()
    {

    }

    public WebItemModuleBeanBuilder(WebItemModuleBean defaultBean)
    {
        super(defaultBean);

        this.link = defaultBean.getLink();
        this.location = defaultBean.getLocation();
        this.context = defaultBean.getContext();
        this.weight = defaultBean.getWeight();
        this.styleClasses = defaultBean.getStyleClasses();
        this.tooltip = defaultBean.getTooltip();
        this.icon = defaultBean.getIcon();
        this.target = defaultBean.getTarget();
    }

    public WebItemModuleBeanBuilder withLink(String link)
    {
        this.link = link;
        return this;
    }

    public WebItemModuleBeanBuilder withContext(AddOnUrlContext context)
    {
        this.context = context;
        return this;
    }

    public WebItemModuleBeanBuilder withLocation(String location)
    {
        this.location = location;
        return this;
    }

    public WebItemModuleBeanBuilder withTarget(WebItemTargetBean target)
    {
        this.target = target;
        return this;
    }

    public WebItemModuleBeanBuilder withWeight(int weight)
    {
        this.weight = weight;
        return this;
    }

    public WebItemModuleBeanBuilder withStyleClasses(List<String> styleClasses)
    {
        this.styleClasses = styleClasses;
        return this;
    }

    public WebItemModuleBeanBuilder withStyleClasses(String... styleClasses)
    {
        this.styleClasses = newArrayList(styleClasses);
        return this;
    }

    public WebItemModuleBeanBuilder withTooltip(I18nProperty tooltip)
    {
        this.tooltip = tooltip;
        return this;
    }

    public WebItemModuleBeanBuilder withIcon(IconBean icon)
    {
        this.icon = icon;
        return this;
    }

    @Override
    public WebItemModuleBean build()
    {
        return new WebItemModuleBean(this);
    }
}
