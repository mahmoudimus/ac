package com.atlassian.plugin.connect.plugin.registry;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.transaction.TransactionCallback;

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
    public ConnectAddonEntity create(final String addonKey, final String settings)
    {
        return ao.executeInTransaction(new TransactionCallback<ConnectAddonEntity>() {
            @Override
            public ConnectAddonEntity doInTransaction()
            {
                return ao.create(ConnectAddonEntity.class, new DBParam(DBPARAM_ADDON_KEY, addonKey), new DBParam(DBPARAM_SETTINGS, settings));
            }
        });
    }

    @Override
    public ConnectAddonEntity createOrUpdate(String addonKey, String settings)
    {
        ConnectAddonEntity addon = update(addonKey, settings);

        if (null == addon)
        {
            addon = create(addonKey, settings);
        }

        return addon;
    }

    @Override
    public ConnectAddonEntity get(final String addonKey)
    {
        ConnectAddonEntity[] addons = ao.executeInTransaction(new TransactionCallback<ConnectAddonEntity[]>()
        {
            @Override
            public ConnectAddonEntity[] doInTransaction()
            {
                return ao.find(ConnectAddonEntity.class, Query.select().where("addon_key = ?", addonKey).limit(1));
            }
        });

        if (null != addons && addons.length > 0)
        {
            return addons[0];
        }

        return null;
    }

    @Override
    public ConnectAddonEntity update(String addonKey, String settings)
    {
        final ConnectAddonEntity addon = get(addonKey);

        if (null != addon)
        {
            addon.setSettings(settings);

            ao.executeInTransaction(new TransactionCallback<Object>()
            {
                @Override
                public Object doInTransaction()
                {
                    addon.save();

                    return null;
                }
            });


            return addon;
        }

        return null;
    }

    @Override
    public void delete(String addonKey)
    {
        final ConnectAddonEntity addon = get(addonKey);

        if (null != addon)
        {
            ao.executeInTransaction(new TransactionCallback<Object>()
            {
                @Override
                public Object doInTransaction()
                {
                    ao.delete(addon);
                    ao.flush(addon);

                    return null;
                }
            });
        }
    }

    @Override
    public Set<String> getAddonKeys()
    {
        final Set<String> keys = new HashSet<String>();

        ao.executeInTransaction(
                new TransactionCallback<Object>()
                {
                    @Override
                    public Object doInTransaction()
                    {
                        ao.stream(ConnectAddonEntity.class, new EntityStreamCallback<ConnectAddonEntity, Integer>()
                        {
                            @Override
                            public void onRowRead(ConnectAddonEntity addon)
                            {
                                keys.add(addon.getAddonKey());
                            }
                        });

                        return null;
                    }
                }
        );
        return ImmutableSet.copyOf(keys);

    }

    @Override
    public Set<ConnectAddonEntity> getAllAddons()
    {
        final Set<ConnectAddonEntity> addons = new HashSet<ConnectAddonEntity>();

        ao.executeInTransaction(
                new TransactionCallback<Object>()
                {
                    @Override
                    public Object doInTransaction()
                    {
                        ao.stream(ConnectAddonEntity.class, new EntityStreamCallback<ConnectAddonEntity, Integer>()
                        {
                            @Override
                            public void onRowRead(ConnectAddonEntity addon)
                            {
                                addons.add(addon);
                            }
                        });
                        
                        return null;
                    }
                }
        );

        return ImmutableSet.copyOf(addons);
    }
}
