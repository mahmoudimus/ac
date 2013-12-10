package com.atlassian.plugin.connect.plugin.capabilities.beans;

public enum WebItemTargetType
{
    page, dialog, inlineDialog;

    @Override
    public String toString()
    {
        return super.toString().toLowerCase();
    }
}
