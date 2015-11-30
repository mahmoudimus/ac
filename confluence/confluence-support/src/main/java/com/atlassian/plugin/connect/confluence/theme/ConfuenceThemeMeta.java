package com.atlassian.plugin.connect.confluence.theme;

import com.atlassian.plugin.connect.modules.beans.ConfluenceThemeModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;

/**
 *
 */
public class ConfuenceThemeMeta extends ConnectModuleMeta<ConfluenceThemeModuleBean>
{
    public ConfuenceThemeMeta()
    {
        super("themes", ConfluenceThemeModuleBean.class);
    }
}
