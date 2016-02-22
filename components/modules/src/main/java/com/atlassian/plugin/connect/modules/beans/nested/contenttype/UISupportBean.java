package com.atlassian.plugin.connect.modules.beans.nested.contenttype;

import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.nested.contenttype.UISupportBeanBuilder;

/**
 * Declares information related for rendering the content in the UI.
 * @since 1.1.77
 */
@SchemaDefinition("extensiveContentTypeUISupport")
public class UISupportBean extends BaseModuleBean
{
    private String contentViewComponent;

    private String contentEditComponent;

    private String containerViewComponent;

    private String titleDisplay;

    private String titleSortValue;

    private IconsBean icons;

    public UISupportBean()
    {
        this(new UISupportBeanBuilder());
    }

    public UISupportBean(UISupportBeanBuilder builder)
    {
        super(builder);
        initialise();
    }

    private void initialise()
    {

    }

    public String getContentViewComponent()
    {
        return contentViewComponent;
    }

    public String getContentEditComponent()
    {
        return contentEditComponent;
    }

    public String getContainerViewComponent()
    {
        return containerViewComponent;
    }

    public String getTitleDisplay()
    {
        return titleDisplay;
    }

    public String getTitleSortValue()
    {
        return titleSortValue;
    }

    public IconsBean getIcons()
    {
        return icons;
    }
}
