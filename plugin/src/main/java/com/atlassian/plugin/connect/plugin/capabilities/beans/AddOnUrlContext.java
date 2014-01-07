package com.atlassian.plugin.connect.plugin.capabilities.beans;

/**
 * @since 1.0
 */
public enum AddOnUrlContext
{
    /**
     * A relative URL whose end target is the addon but initially targets the host so the iFrame can be decorated.
     */
    decorated,

    /**
     * A URL that is relative to the addon host.
     */
    addon,

    /**
     * A URL that is relative to the host product.
     */
    product;

    @Override
    public String toString()
    {
        return super.toString().toLowerCase();
    }
}
