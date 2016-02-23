package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.ControlBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroPropertyPanelBean;

import java.util.List;

public class MacroPropertyPanelBeanBuilder extends BaseModuleBeanBuilder<MacroPropertyPanelBeanBuilder, MacroPropertyPanelBean>
{
    private String url;

    private List<ControlBean> controls;

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

    public MacroPropertyPanelBeanBuilder withControls(List<ControlBean> controlBeans)
    {
        this.controls = controlBeans;
        return this;
    }

    @Override
    public MacroPropertyPanelBean build()
    {
        return new MacroPropertyPanelBean(this);
    }
}
