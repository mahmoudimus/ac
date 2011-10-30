package com.atlassian.labs.remoteapps.modules.applinks;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.spi.application.NonAppLinksEntityType;
import com.atlassian.applinks.spi.application.TypeId;

import java.net.URI;

/**
 *
 */
public class RemoteAppEntityType implements NonAppLinksEntityType
{
    private final TypeId typeId;
    private final Class<? extends RemoteAppApplicationType> applicationType;
    private final String i18nKey;
    private final String pluralizedI18nKey;
    private final URI iconUrl;

    public RemoteAppEntityType(TypeId typeId, Class<? extends RemoteAppApplicationType> applicationType, String i18nKey, String pluralizedI18nKey, URI iconUrl)
    {
        this.typeId = typeId;
        this.applicationType = applicationType;
        this.i18nKey = i18nKey;
        this.pluralizedI18nKey = pluralizedI18nKey;
        this.iconUrl = iconUrl;
    }

    @Override
    public Class<? extends ApplicationType> getApplicationType()
    {
        return applicationType;
    }

    @Override
    public String getI18nKey()
    {
        return i18nKey;
    }

    @Override
    public String getPluralizedI18nKey()
    {
        return pluralizedI18nKey;
    }

    @Override
    public String getShortenedI18nKey()
    {
        return i18nKey;
    }

    @Override
    public URI getIconUrl()
    {
        return iconUrl;
    }

    @Override
    public URI getDisplayUrl(ApplicationLink link, String entityKey)
    {
        return URI.create(link.getDisplayUrl().toString() + "/" + entityKey);
    }

    @Override
    public TypeId getId()
    {
        return typeId;
    }
}
