package com.atlassian.plugin.connect.api;

public interface ConnectAddonController
{
    /**
     * Enables the provided Connect add-on, if installed.
     *
     * @param addonKey keys of the add-on to enable
     * @throws ConnectAddonEnableException              
     */
    void enableAddon(String addonKey) throws ConnectAddonEnableException;

    /**
     * Disables the provided Connect add-on.
     *
     * @param addonKey key of the add-on to disable
     */
    void disableAddon(String addonKey);

    /**
     * Installs the provided Connect add-on.
     *
     * @param jsonDescriptor JSON descriptors of the add-on to install
     * @throws ConnectAddonInstallException
     */
    void installAddon(String jsonDescriptor) throws ConnectAddonInstallException;

    /**
     * Uninstalls the provided Connect add-on.
     *
     * @param addonKey key of the add-on to uninstall
     */
    void uninstallAddon(String addonKey);
}
