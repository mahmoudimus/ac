package com.atlassian.plugin.connect.plugin.capabilities.beans;

/**
 * @since 1.0
 */
public enum AddOnUrlContext
{
    addon, addonDirect, product;

    @Override
    public String toString()
    {
        return super.toString().toLowerCase();
    }
}
