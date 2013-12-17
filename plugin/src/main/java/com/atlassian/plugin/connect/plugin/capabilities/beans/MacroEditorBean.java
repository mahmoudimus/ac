package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.MacroEditorBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.google.common.base.Strings;

/**
 * TODO
 */
public class MacroEditorBean extends BaseModuleBean
{
    @Required
    private String url;
    private I18nProperty editTitle;
    private I18nProperty insertTitle;
    private String width;
    private String height;

    public MacroEditorBean()
    {
        init();
    }

    public MacroEditorBean(MacroEditorBeanBuilder builder)
    {
        super(builder);
        init();
    }

    private void init()
    {
        if (null == url)
        {
            url = "";
        }
        if (null == editTitle)
        {
            this.editTitle = I18nProperty.empty();
        }
        if (null == insertTitle)
        {
            this.insertTitle = I18nProperty.empty();
        }
        if (null == width)
        {
            width = "";
        }
        if (null == height)
        {
            height = "";
        }
    }

    public String getUrl()
    {
        return url;
    }

    public boolean hasUrl()
    {
        return !Strings.isNullOrEmpty(url);
    }

    public I18nProperty getEditTitle()
    {
        return editTitle;
    }

    public boolean hasEditTitle()
    {
        return !Strings.isNullOrEmpty(editTitle.getValue());
    }

    public I18nProperty getInsertTitle()
    {
        return insertTitle;
    }

    public boolean hasInsertTitle()
    {
        return !Strings.isNullOrEmpty(insertTitle.getValue());
    }

    public String getWidth()
    {
        return width;
    }

    public String getHeight()
    {
        return height;
    }

    public static MacroEditorBeanBuilder newMacroEditorBean()
    {
        return new MacroEditorBeanBuilder();
    }

    public static MacroEditorBeanBuilder newMacroEditorBean(MacroEditorBean defaultBean)
    {
        return new MacroEditorBeanBuilder(defaultBean);
    }
}
