package com.atlassian.plugin.connect.modules.beans;

import java.util.List;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.ConfluenceThemeModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.UiOverrideBean;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.copyFieldsByNameAndType;

/**
 *
 * @since 1.1.62
 */
public class ConfluenceThemeModuleBean extends RequiredKeyBean
{
//    @Required
//    private I18nProperty description;

    @Required
    private List<UiOverrideBean> overrides;

    public ConfluenceThemeModuleBean(ConfluenceThemeModuleBeanBuilder builder)
    {
        super(builder);
        copyFieldsByNameAndType(builder, this);
    }

    public static ConfluenceThemeModuleBeanBuilder newBlueprintModuleBean()
    {
        return new ConfluenceThemeModuleBeanBuilder();
    }

    public List<UiOverrideBean> getOverrides()
    {
        return overrides;
    }

//    public I18nProperty getDescription()
//    {
//        return description;
//    }
}
