package com.atlassian.plugin.connect.api;

import com.atlassian.annotations.PublicApi;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;

import java.util.Collection;
import java.util.Optional;

/**
 * The accessor of Atlassian Connect add-on state.
 */
@PublicApi
public interface ConnectAddonAccessor
{
    /**
     * Checks if the Connect add-on is installed and enabled.
     *
     * @param addonKey key of the add-on to check
     * @return true if the Connect add-on is installed and enabled, otherwise false
     */
    boolean isAddonEnabled(String addonKey);

    /**
     * Returns the add-on with the given key, if installed.
     * @param addonKey the key of the add-on
     * @return the add-on or empty
     */
    public Optional<ConnectAddonBean> getAddon(String addonKey);

    /**
     * Returns the keys of all installed add-ons.
     *
     * @return the keys of all installed add-ons
     */
    public Collection<String> getAllAddonKeys();

    /**
     * Returns all installed add-ons.
     *
     * @return the installed add-ons
     */
    public Collection<ConnectAddonBean> getAllAddons();
}
