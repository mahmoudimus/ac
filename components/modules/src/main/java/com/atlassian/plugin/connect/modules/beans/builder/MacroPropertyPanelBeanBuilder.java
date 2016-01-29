package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.MacroPropertyPanelBean;

public class MacroPropertyPanelBeanBuilder extends BaseModuleBeanBuilder<MacroPropertyPanelBeanBuilder, MacroPropertyPanelBean>
{
    private String url;

    public MacroPropertyPanelBeanBuilder()
    {
    }

    public MacroPropertyPanelBeanBuilder(MacroPropertyPanelBean defaultBean)
    {
        this.url = defaultBean.getUrl();
    }

    public MacroPropertyPanelBeanBuilder withUrl(String url)
    {
        this.url = url;
        return this;
    }

    @Override
    public MacroPropertyPanelBean build()
    {
        return new MacroPropertyPanelBean(this);
    }
}
