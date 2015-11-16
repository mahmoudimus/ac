package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.SchemaDefinition;

/**
 * @since 1.0
 */
@SchemaDefinition("urlContext")
public enum AddOnUrlContext
{
    /**
     * A context that uses the url as the module key to point to an existing page
     */
    page,

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
