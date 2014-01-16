package com.atlassian.plugin.connect.plugin.iframe.render.strategy;

/**
 * Responsible for handling registration of iframe {@link IFrameRenderStrategy render strategies}.
 */
public interface IFrameRenderStrategyRegistry
{
    void register(String addonKey, String moduleKey, IFrameRenderStrategy renderStrategy);

    /**
     * Unregister any iframe {@link IFrameRenderStrategy render strategies} registered under the supplied key.
     *
     * @param addonKey the key of an addon that has just been disabled.
     */
    void unregisterAll(String addonKey);

    /**
     * @param addonKey the key of an addon
     * @param moduleKey the key of a module that supplies an render strategy
     * @return the endpoint if there is one registered for the specified keys, otherwise null.
     */
    IFrameRenderStrategy get(String addonKey, String moduleKey);
}
