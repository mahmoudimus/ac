package com.atlassian.plugin.connect.plugin.registry;

import java.util.Set;

import com.atlassian.activeobjects.tx.Transactional;

@Transactional
public interface ConnectAddonEntityService
{
    public static final String DBPARAM_ADDON_KEY = "ADDON_KEY";
    public static final String DBPARAM_SETTINGS = "SETTINGS";
    
    ConnectAddonEntity create(String addonKey, String settings);
    ConnectAddonEntity createOrUpdate(String addonKey, String settings);
    ConnectAddonEntity get(String addonKey);
    ConnectAddonEntity update(String addonKey, String settings);
    void delete(String addonKey);
    Set<String> getAddonKeys();
    Set<ConnectAddonEntity> getAllAddons();
    
}
