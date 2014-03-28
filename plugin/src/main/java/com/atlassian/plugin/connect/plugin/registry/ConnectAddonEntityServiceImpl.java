package com.atlassian.plugin.connect.plugin.registry;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.activeobjects.external.ActiveObjects;

import com.google.common.collect.ImmutableSet;

import net.java.ao.DBParam;
import net.java.ao.EntityStreamCallback;
import net.java.ao.Query;

@Named
public class ConnectAddonEntityServiceImpl implements ConnectAddonEntityService
{
    private final ActiveObjects ao;

    @Inject
    public ConnectAddonEntityServiceImpl(ActiveObjects ao)
    {
        this.ao = ao;
    }

    @Override
    public ConnectAddonEntity create(String addonKey, String settings)
    {
        return ao.create(ConnectAddonEntity.class, new DBParam(DBPARAM_ADDON_KEY, addonKey), new DBParam(DBPARAM_SETTINGS, settings));
    }

    @Override
    public ConnectAddonEntity createOrUpdate(String addonKey, String settings)
    {
        ConnectAddonEntity addon = update(addonKey,settings);

        if (null == addon)
        {
            addon = create(addonKey,settings);
        }

        return addon;
    }

    @Override
    public ConnectAddonEntity get(String addonKey)
    {
        ConnectAddonEntity[] addons = ao.find(ConnectAddonEntity.class, Query.select().where("addon_key = ?", addonKey).limit(1));

        if (null != addons && addons.length > 0)
        {
            return addons[0];
        }

        return null;
    }

    @Override
    public ConnectAddonEntity update(String addonKey, String settings)
    {
        ConnectAddonEntity addon = get(addonKey);

        if (null != addon)
        {
            addon.setSettings(settings);
            addon.save();

            return addon;
        }

        return null;
    }

    @Override
    public void delete(String addonKey)
    {
        ConnectAddonEntity addon = get(addonKey);

        if (null != addon)
        {
            ao.delete(addon);
            ao.flush(addon);
        }
    }

    @Override
    public Set<String> getAddonKeys()
    {
        final Set<String> keys = new HashSet<String>();
        
        ao.stream(ConnectAddonEntity.class,new EntityStreamCallback<ConnectAddonEntity, Integer>() {
            @Override
            public void onRowRead(ConnectAddonEntity addon)
            {
                keys.add(addon.getAddonKey());
            }
        });

        return ImmutableSet.copyOf(keys);

    }

    @Override
    public Set<ConnectAddonEntity> getAllAddons()
    {
        final Set<ConnectAddonEntity> addons = new HashSet<ConnectAddonEntity>();

        ao.stream(ConnectAddonEntity.class,new EntityStreamCallback<ConnectAddonEntity, Integer>() {
            @Override
            public void onRowRead(ConnectAddonEntity addon)
            {
                addons.add(addon);
            }
        });

        return ImmutableSet.copyOf(addons);
    }
}
