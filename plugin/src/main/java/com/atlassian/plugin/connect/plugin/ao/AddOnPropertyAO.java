package com.atlassian.plugin.connect.plugin.ao;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import net.java.ao.RawEntity;
import net.java.ao.schema.Indexed;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.PrimaryKey;
import net.java.ao.schema.StringLength;

/**
 * Active object representation of an add-on property.
 * @see com.atlassian.plugin.connect.plugin.ao.AddOnProperty
 *
 * @since TODO: fill in the proper version before merge
 */
public interface AddOnPropertyAO extends RawEntity<String>
{
    int MAXIMUM_PROPERTY_KEY_LENGTH = 255;
    // we need to have a primary key consisting of both plugin key and property key
    // not possible to have a multi-column: https://ecosystem.atlassian.net/browse/AO-96
    // since this is impossible, we had to create a new field that will have to be manually filled with the joined string.

    // joined plugin key with property key separated by colon
    @PrimaryKey
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
