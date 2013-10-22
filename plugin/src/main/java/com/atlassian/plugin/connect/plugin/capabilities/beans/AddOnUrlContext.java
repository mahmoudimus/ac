package com.atlassian.plugin.connect.plugin.capabilities.beans;

/**
 * @since version
 */
public enum AddOnUrlContext
{
    addon, product;


    @Override
    public String toString()
    {
        return super.toString().toLowerCase();
    }
}
