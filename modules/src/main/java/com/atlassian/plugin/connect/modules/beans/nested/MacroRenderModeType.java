package com.atlassian.plugin.connect.modules.beans.nested;

public enum MacroRenderModeType {
    mobile, desktop;

    @Override
    public String toString()
    {
        return super.toString().toLowerCase();
    }

}
