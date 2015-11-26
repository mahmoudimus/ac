package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.EmbeddedStaticContentMacroBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroRenderModesBean;

public class MacroRenderModesBeanBuilder extends BaseModuleBeanBuilder<MacroRenderModesBeanBuilder, MacroRenderModesBean>
{
    private EmbeddedStaticContentMacroBean word;
    private EmbeddedStaticContentMacroBean pdf;
    private EmbeddedStaticContentMacroBean htmlExport;
    private EmbeddedStaticContentMacroBean feed;
    private EmbeddedStaticContentMacroBean email;
    private EmbeddedStaticContentMacroBean defaultFallback;
        
    @Override
    public MacroRenderModesBean build()
    {
        return new MacroRenderModesBean(this);
    }

    public MacroRenderModesBeanBuilder withWord(EmbeddedStaticContentMacroBean bean)
    {
        this.word = bean;
        return this;
    }
    public MacroRenderModesBeanBuilder withPdf(EmbeddedStaticContentMacroBean bean)
    {
        this.pdf = bean;
        return this;
    }
    public MacroRenderModesBeanBuilder withFeed(EmbeddedStaticContentMacroBean bean)
    {
        this.feed = bean;
        return this;
    }
    public MacroRenderModesBeanBuilder withEmail(EmbeddedStaticContentMacroBean bean)
    {
        this.email = bean;
        return this;
    }
    public MacroRenderModesBeanBuilder withDefaultfallback(EmbeddedStaticContentMacroBean bean)
    {
        this.defaultFallback = bean;
        return this;
    }
    public MacroRenderModesBeanBuilder withHtmlExport(EmbeddedStaticContentMacroBean bean)
    {
        this.htmlExport = bean;
        return this;
    }

}
