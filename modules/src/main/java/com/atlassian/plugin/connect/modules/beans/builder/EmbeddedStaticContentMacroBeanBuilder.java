package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.EmbeddedStaticContentMacroBean;

public class EmbeddedStaticContentMacroBeanBuilder extends BaseModuleBeanBuilder<EmbeddedStaticContentMacroBeanBuilder, EmbeddedStaticContentMacroBean>
{
    private String url;

    public EmbeddedStaticContentMacroBeanBuilder()
    {
    }

    public EmbeddedStaticContentMacroBeanBuilder(EmbeddedStaticContentMacroBean defaultBean)
    {
        this.url = defaultBean.getUrl();
    }

    @Override
    public EmbeddedStaticContentMacroBean build()
    {
        return new EmbeddedStaticContentMacroBean(this);
    }

    public EmbeddedStaticContentMacroBeanBuilder withUrl(String url)
    {
        this.url = url;
        return this;
    }
}
