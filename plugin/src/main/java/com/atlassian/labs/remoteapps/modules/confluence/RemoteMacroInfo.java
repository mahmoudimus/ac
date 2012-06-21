package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.util.contextparameter.RequestContextParameterFactory;
import org.dom4j.Element;

/**
 * Information about the specific macro type
 */
public class RemoteMacroInfo
{
    private final Element element;
    private final ApplicationLinkOperationsFactory.LinkOperations linkOperations;
    private final Macro.BodyType bodyType;
    private final Macro.OutputType outputType;
    private final RequestContextParameterFactory requestContextParameterFactory;
    private final String url;

    public RemoteMacroInfo(
            Element element, ApplicationLinkOperationsFactory.LinkOperations linkOperations,
            Macro.BodyType bodyType,
            Macro.OutputType outputType, RequestContextParameterFactory requestContextParameterFactory, String url)
    {
        this.element = element;
        this.linkOperations = linkOperations;
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

    public ApplicationLinkOperationsFactory.LinkOperations getApplicationLinkOperations()
    {
        return linkOperations;
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
