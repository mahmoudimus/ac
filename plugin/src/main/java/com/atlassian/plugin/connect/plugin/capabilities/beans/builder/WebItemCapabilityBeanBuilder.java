package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.beans.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.IconCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;

/**
 * @since version
 */
public class WebItemCapabilityBeanBuilder<T extends WebItemCapabilityBeanBuilder, B extends WebItemCapabilityBean> extends BaseCapabilityBeanBuilder<T, B>
{
    private String link;
    private String section;
    private int weight;
    private List<String> styleClasses;
    private I18nProperty tooltip;
    private IconCapabilityBean icon;

    public WebItemCapabilityBeanBuilder()
    {

    }

    public WebItemCapabilityBeanBuilder(WebItemCapabilityBean defaultBean)
    {
        super(defaultBean);

        this.link = defaultBean.getLink();
        this.section = defaultBean.getSection();
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

    public WebItemCapabilityBeanBuilder withSection(String section)
    {
        this.section = section;
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

    public WebItemCapabilityBeanBuilder withTooltip(I18nProperty tooltip)
    {
        this.tooltip = tooltip;
        return this;
    }

    public WebItemCapabilityBeanBuilder withIcon(IconCapabilityBean icon)
    {
        this.icon = icon;
        return this;
    }

    @Override
    public B build()
    {
        return (B) new WebItemCapabilityBean(this);
    }
}
