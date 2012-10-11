package com.atlassian.plugin.remotable.plugin.module;

import java.util.Map;

public class InsertMacroWebItemContext implements WebItemContext
{
    @Override
    public Map<String, String> getContextParams()
    {
        return null;
    }

    @Override
    public int getPreferredWeight()
    {
        return 10;
    }

    @Override
    public String getPreferredSectionKey()
    {
        return "system.editor.featured.macros.default";
    }
}
