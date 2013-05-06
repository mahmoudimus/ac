package com.atlassian.plugin.remotable.plugin.module.confluence;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.plugin.remotable.plugin.util.contextparameter.RequestContextParameterFactory;
import com.atlassian.plugin.remotable.plugin.util.node.Node;

import java.net.URI;

/**
 * Information about the specific macro type
 */
public class RemoteMacroInfo
{
    private final Node element;
    private final String pluginKey;
    private final Macro.BodyType bodyType;
    private final Macro.OutputType outputType;
    private final RequestContextParameterFactory requestContextParameterFactory;
    private final URI url;

    public RemoteMacroInfo(
            Node element, String pluginKey,
            Macro.BodyType bodyType,
            Macro.OutputType outputType, RequestContextParameterFactory requestContextParameterFactory, URI url)
    {
        this.element = element;
        this.pluginKey = pluginKey;
        this.bodyType = bodyType;
        this.outputType = outputType;
        this.requestContextParameterFactory = requestContextParameterFactory;
        this.url = url;
    }

    public Macro.BodyType getBodyType()
    {
        return bodyType;
    }

    public Macro.OutputType getOutputType()
    {
        return outputType;
    }

    public URI getUrl()
    {
        return url;
    }

    public String getPluginKey()
    {
        return pluginKey;
    }

    public Node getElement()
    {
        return element;
    }

    public RequestContextParameterFactory getRequestContextParameterFactory()
    {
        return requestContextParameterFactory;
    }
}
