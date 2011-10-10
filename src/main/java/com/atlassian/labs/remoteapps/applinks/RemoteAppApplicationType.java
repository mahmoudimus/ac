package com.atlassian.labs.remoteapps.applinks;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;

import java.net.URI;

/**
 *
 */
public class RemoteAppApplicationType implements NonAppLinksApplicationType
{
    private final TypeId typeId;
    private final String label;
    private final URI icon;
    private ApplicationLinkDetails details;

    public RemoteAppApplicationType(TypeId typeId, String label, URI icon, ApplicationLinkDetails details)
    {
        this.typeId = typeId;
        this.label = label;
        this.icon = icon;
        this.details = details;
    }

    @Override
    public String getI18nKey()
    {
        return label;
    }

    @Override
    public URI getIconUrl()
    {
        return icon;
    }

    @Override
    public TypeId getId()
    {
        return typeId;
    }

    public ApplicationLinkDetails getDefaultDetails()
    {
        return details;
    }
}
