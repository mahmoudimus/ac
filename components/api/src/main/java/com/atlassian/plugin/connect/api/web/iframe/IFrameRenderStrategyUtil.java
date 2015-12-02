package com.atlassian.plugin.connect.api.web.iframe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Optional;

public class IFrameRenderStrategyUtil
{

    public static String renderToString(Map<String, String> moduleContextParameters, IFrameRenderStrategy iFrameRenderStrategy)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            iFrameRenderStrategy.render(moduleContextParameters, new OutputStreamWriter(out), Optional.empty());
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
