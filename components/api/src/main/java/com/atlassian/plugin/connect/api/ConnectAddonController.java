package com.atlassian.plugin.connect.api;

public interface ConnectAddonController
{
    /**
     * Enables the provided Connect add-ons, if installed.
     *
     * @param addonKeys keys of the add-ons to enable
     */
    void enableAddons(String... addonKeys);

    /**
     * Disables the provided Connect add-on.
     *
     * @param addonKey key of the add-on to disable
     * @throws Exception                 
     */
    void disableAddon(String addonKey) throws Exception;

    /**
     * Disables the provided Connect add-on.
     * Does not persist the add-on state. 
     *
     * @param addonKey key of the add-on to disable
     * @throws Exception
     */
    void disableAddonWithoutPersistingState(String addonKey) throws Exception;

    /**
     * Installs the provided Connect add-ons.
     *
     * @param jsonDescriptors JSON descriptors of the add-ons to install
     */
    void installAddons(String... jsonDescriptors);


    /**
     * Uninstalls the provided Connect add-on.
     *
     * @param addonKey key of the add-on to uninstall
     * @throws Exception
     */
    void uninstallAddon(String addonKey) throws Exception;
}
