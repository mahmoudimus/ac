package com.atlassian.plugin.connect.modules.beans;

import java.util.List;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.ConfluenceThemeModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.UiOverrideBean;

/**
 *
 * @since 1.1.62
 */
public class ConfluenceThemeModuleBean extends RequiredKeyBean
{
    @Required
    private List<UiOverrideBean> overrides;

    public static ConfluenceThemeModuleBeanBuilder newBlueprintModuleBean()
    {
        return new ConfluenceThemeModuleBeanBuilder();
    }

    public List<UiOverrideBean> getOverrides() {
        return overrides;
    }
}
