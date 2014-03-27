package com.atlassian.plugin.connect.plugin.registry;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.*;

@Preload
@Table("ConnectAddonEntity")
public interface ConnectAddonEntity extends Entity
{
    @NotNull //must have a value
    @Indexed //index the names
    @Unique //must be unique
    public void setAddonKey(String addonKey);

    public String getAddonKey();

    @NotNull //must have a value
    @StringLength(StringLength.UNLIMITED) //unlimited makes it a CLOB
    public void setSettings(String settings);
    public String getSettings();
}
