package com.atlassian.plugin.connect.plugin.capabilities.beans;

/**
 * @since 1.0
 */
public enum WebItemTarget
{
    none,           // default browser handling of links
    dialog,         // open target in a modal dialog
    inlineDialog;   // open target in an inline dialog

    @Override
    public String toString()
    {
        return super.toString().toLowerCase();
    }
}
