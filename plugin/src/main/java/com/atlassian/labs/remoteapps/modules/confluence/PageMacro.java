package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.labs.remoteapps.modules.IFrameRenderer;
import com.atlassian.labs.remoteapps.modules.page.IFrameContext;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Function;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class PageMacro extends AbstractRemoteMacro
{
    private final UserManager userManager;
    private final IFrameContext iframeContext;
    private final IFrameRenderer iFrameRenderer;

    public PageMacro(RemoteMacroInfo remoteMacroInfo, UserManager userManager,
            IFrameRenderer iFrameRenderer, IFrameContext iframeContext)
    {
        super(remoteMacroInfo);
        this.userManager = userManager;
        this.iframeContext = iframeContext;
        this.iFrameRenderer = iFrameRenderer;
    }

    @Override
    public String execute(Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext) throws MacroExecutionException
    {
        String remoteUser = userManager.getRemoteUsername();
        String counter = incrementCounter(conversionContext);
        try
        {
             return iFrameRenderer.render(new IFrameContext(iframeContext, "-" + counter), "", convertParams(parameters),
                            remoteUser);

        } catch (IOException e)
        {
            throw new MacroExecutionException(e);
        }
    }

    private String incrementCounter(ConversionContext ctx)
    {
        String key = "__counter_" + iframeContext.getNamespace();
        Integer counter = (Integer) ctx.getProperty(key);
        counter = counter == null ? 0 : counter+1;
        ctx.setProperty(key, counter);

        return counter.toString();
    }

    private Map<String, String[]> convertParams(Map<String, String> parameters)
    {
        return Maps.transformValues(parameters, new Function<String,String[]>()
        {

            @Override
            public String[] apply(String from)
            {
                return new String[] {from};
            }
        });
    }

}
