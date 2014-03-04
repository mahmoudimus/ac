package com.atlassian.plugin.connect.plugin.iframe.render.strategy;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 *
 */
public class IFrameRenderStrategyUtil
{

    public static String renderToString(ModuleContextParameters moduleContextParameters, IFrameRenderStrategy iFrameRenderStrategy)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            iFrameRenderStrategy.render(moduleContextParameters, new OutputStreamWriter(out), Option.<String>none());
        }
        catch (IOException e)
        {
            // no I/O, so no IOException.. right?
            throw new IllegalStateException(e);
        }
        return out.toString();
    }

    public static String renderAccessDeniedToString(IFrameRenderStrategy iFrameRenderStrategy)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            iFrameRenderStrategy.renderAccessDenied(new OutputStreamWriter(out));
        }
        catch (IOException e)
        {
            // no I/O, so no IOException.. right?
            throw new IllegalStateException(e);
        }
        return out.toString();
    }


}
