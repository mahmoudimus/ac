package com.atlassian.plugin.connect.plugin.iframe.render.strategy;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public interface IFrameRequestProcessor
{
    void process(HttpServletRequest request);
}
