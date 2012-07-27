package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.labs.remoteapps.RemoteAppAccessor;
import com.atlassian.labs.remoteapps.RemoteAppAccessorFactory;
import com.atlassian.labs.remoteapps.util.contextparameter.RequestContextParameterFactory;
import org.dom4j.Element;

/**
 * Information about the specific macro type
 */
public class RemoteMacroInfo
{
    private final Element element;
    private final RemoteAppAccessor remoteAppAccessor;
    private final Macro.BodyType bodyType;
    private final Macro.OutputType outputType;
    private final RequestContextParameterFactory requestContextParameterFactory;
    private final String url;

    public RemoteMacroInfo(
            Element element, RemoteAppAccessor remoteAppAccessor,
            Macro.BodyType bodyType,
            Macro.OutputType outputType, RequestContextParameterFactory requestContextParameterFactory, String url)
    {
        this.element = element;
        this.remoteAppAccessor = remoteAppAccessor;
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

    public String getUrl()
    {
        return url;
    }

    public RemoteAppAccessor getRemoteAppAccessor()
    {
        return remoteAppAccessor;
    }

    public Element getElement()
    {
        return element;
    }

    public RequestContextParameterFactory getRequestContextParameterFactory()
    {
        return requestContextParameterFactory;
    }
}
