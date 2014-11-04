package com.atlassian.plugin.connect.plugin.capabilities.module.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyUtil;

import java.util.Map;

public final class DynamicContentMacro extends AbstractMacro
{
    private final IFrameRenderStrategy iFrameRenderStrategy;
    private final MacroModuleContextExtractor macroModuleContextExtractor;

    public DynamicContentMacro(BodyType bodyType, OutputType outputType, IFrameRenderStrategy iFrameRenderStrategy,
            MacroModuleContextExtractor macroModuleContextExtractor)
    {
        super(bodyType, outputType);
        this.iFrameRenderStrategy = iFrameRenderStrategy;
        this.macroModuleContextExtractor = macroModuleContextExtractor;
    }

    @Override
    public String execute(Map<String, String> parameters, String storageFormatBody, ConversionContext conversionContext)
            throws MacroExecutionException
    {
        ModuleContextParameters moduleContext = macroModuleContextExtractor.extractParameters(storageFormatBody, conversionContext, parameters);
        return IFrameRenderStrategyUtil.renderToString(moduleContext, iFrameRenderStrategy);
    }

}
