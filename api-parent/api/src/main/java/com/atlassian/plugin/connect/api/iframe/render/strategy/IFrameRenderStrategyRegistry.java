package com.atlassian.plugin.connect.api.iframe.render.strategy;

/**
 * Responsible for handling registration of iframe {@link com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategy render strategies}.
 */
public interface IFrameRenderStrategyRegistry
{
    void register(String addonKey, String moduleKey, IFrameRenderStrategy renderStrategy);

    void register(String addonKey, String moduleKey, String classifier, IFrameRenderStrategy renderStrategy);

    /**
     * Unregister any iframe {@link IFrameRenderStrategy render strategies} registered under the supplied key.
     *
     * @param addonKey the key of an addon that has just been disabled.
     */
    void unregisterAll(String addonKey);

    /**
     * @param addonKey the key of an addon
     * @param moduleKey the key of a module that supplies an render strategy
     * @return the endpoint if there is one registered for the specified keys, otherwise null
     */
    IFrameRenderStrategy get(String addonKey, String moduleKey);

    IFrameRenderStrategy get(String addonKey, String moduleKey, String classifier);

    /**
     * @param addonKey the key of an addon
     * @param moduleKey the key of a module that supplies an render strategy
     * @return the endpoint if there is one registered for the specified keys
     * @throws java.lang.IllegalStateException if no render strategy exists for the specified key
     */
    IFrameRenderStrategy getOrThrow(String addonKey, String moduleKey) throws IllegalStateException;

    IFrameRenderStrategy getOrThrow(String addonKey, String moduleKey, String classifier) throws IllegalStateException;
}
