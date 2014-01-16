package com.atlassian.plugin.connect.plugin.iframe.render.strategy;

/**
 *
 */
public interface IFrameRenderStrategyFactory
{
    IFrameRenderStrategy page(String addOnKey, String moduleKey, String uriTemplate, String template, String decorator,
                              String title);
}
