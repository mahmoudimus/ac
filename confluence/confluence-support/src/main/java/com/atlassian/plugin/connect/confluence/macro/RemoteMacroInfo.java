package com.atlassian.plugin.connect.confluence.macro;

import java.net.URI;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.plugin.connect.api.util.RequestContextParameterFactory;
import com.atlassian.plugin.connect.spi.http.HttpMethod;

import org.dom4j.Element;

/**
 * Information about the specific macro type
 */
public class RemoteMacroInfo
{
    private final Element element;
    private final String pluginKey;
    private final Macro.BodyType bodyType;
    private final Macro.OutputType outputType;
    private final RequestContextParameterFactory requestContextParameterFactory;
    private final URI url;
    private final HttpMethod httpMethod;

    public RemoteMacroInfo(Element element,
                           String pluginKey,
                           Macro.BodyType bodyType,
                           Macro.OutputType outputType,
                           RequestContextParameterFactory requestContextParameterFactory,
                           URI url,
                           HttpMethod httpMethod)
    {
        this.element = element;
        this.pluginKey = pluginKey;
        this.bodyType = bodyType;
        this.outputType = outputType;
        this.requestContextParameterFactory = requestContextParameterFactory;
        this.url = url;
        this.httpMethod = httpMethod;
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

    public Element getElement()
    {
        return element;
    }

    public RequestContextParameterFactory getRequestContextParameterFactory()
    {
        return requestContextParameterFactory;
    }

    public HttpMethod getHttpMethod()
    {
        return httpMethod;
    }
}
