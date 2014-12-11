package com.atlassian.plugin.connect.plugin.ao;

import net.java.ao.Entity;
import net.java.ao.RawEntity;
import net.java.ao.schema.Indexed;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.PrimaryKey;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.3
 */
public interface AddOnPropertyAO extends RawEntity<String>
{
    // not possible to have a multi-column: https://ecosystem.atlassian.net/browse/AO-96

    // joined plugin key with property key
    @PrimaryKey
    String getPrimaryKey();
    void setPrimaryKey(String primaryKey);

    @Indexed
    @NotNull
    String getPluginKey();
    void setPluginKey(String key);

    @NotNull
    String getPropertyKey();
    void setPropertyKey(String key);

    @NotNull
    String getValue();
    void setValue(String value);
}
