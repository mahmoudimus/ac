package com.atlassian.plugin.connect.plugin.property;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

import net.java.ao.Entity;
import net.java.ao.schema.Indexed;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;
import net.java.ao.schema.Unique;

/**
 * Active object representation of an add-on property.
 * @see com.atlassian.plugin.connect.plugin.property.AddonProperty
 */

// The 'On' in 'AddOn' is deliberately capitalised for backwards compatibility.
@Table ("AddOnPropertyAO" /* Do not change the value or case of this string */)
public interface AddonPropertyAO extends Entity
{
    int MAXIMUM_PROPERTY_KEY_LENGTH = 127;
    // we need to have a primary key consisting of both plugin key and property key
    // not possible to have a multi-column: https://ecosystem.atlassian.net/browse/AO-96
    // since this is impossible, we had to create a new field that will have to be manually filled with the joined string.

    // joined plugin key with property key separated by colon
    // Due to MySQL database, "MAXIMUM_PROPERTY_KEY_LENGTH + ConnectAddonBean.MAX_KEY_LENGTH + 1" cannot be greater than 255 (Ref ACDEV-2076)
    @Unique
    @StringLength(MAXIMUM_PROPERTY_KEY_LENGTH + ConnectAddonBean.MAX_KEY_LENGTH + 1)
    String getPrimaryKey();
    void setPrimaryKey(String primaryKey);

    @Indexed
    @NotNull
    @StringLength(ConnectAddonBean.MAX_KEY_LENGTH)
    String getPluginKey();
    void setPluginKey(String key);

    @NotNull
    @StringLength(MAXIMUM_PROPERTY_KEY_LENGTH)
    String getPropertyKey();
    void setPropertyKey(String key);

    @NotNull
    @StringLength(StringLength.UNLIMITED)
    String getValue();
    void setValue(String value);

}
