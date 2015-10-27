package com.atlassian.plugin.connect.modules.beans;

public enum WebItemTargetType
{
    page, dialog, inlineDialog;

    @Override
    public String toString()
    {
        return super.toString().toLowerCase();
    }
}
