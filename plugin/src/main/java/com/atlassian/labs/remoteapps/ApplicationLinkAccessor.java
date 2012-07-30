package com.atlassian.labs.remoteapps;

import com.atlassian.applinks.api.*;
import com.atlassian.applinks.spi.application.ApplicationIdUtil;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.labs.remoteapps.modules.applinks.ApplicationTypeClassLoader;
import com.atlassian.labs.remoteapps.modules.applinks.RemoteAppApplicationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang.Validate.notNull;

/**
 * Accesses an application link
 */
@Component
public class ApplicationLinkAccessor
{
    private final ApplicationTypeClassLoader applicationTypeClassLoader;
    private final ApplicationLinkService applicationLinkService;
    private final TypeAccessor typeAccessor;

    @Autowired
    public ApplicationLinkAccessor(ApplicationTypeClassLoader applicationTypeClassLoader,
                                   ApplicationLinkService applicationLinkService, TypeAccessor typeAccessor)
    {
        this.applicationTypeClassLoader = applicationTypeClassLoader;
        this.applicationLinkService = applicationLinkService;
        this.typeAccessor = typeAccessor;
    }

    public ApplicationLink getApplicationLink(ApplicationType applicationType)
    {
        ApplicationLink link = applicationLinkService.getPrimaryApplicationLink(applicationType.getClass());
        if (link == null && applicationType instanceof RemoteAppApplicationType)
        {
            RemoteAppApplicationType raType = (RemoteAppApplicationType) applicationType;
            ApplicationId id = ApplicationIdUtil.generate(raType.getDefaultDetails().getRpcUrl());
            try
            {
                link = applicationLinkService.getApplicationLink(id);
            }
            catch (TypeNotInstalledException e)
            {
                throw new RuntimeException("Type should always be installed", e);
            }
        }
//        notNull(link);
        return link;
    }

    public RemoteAppApplicationType getApplicationTypeIfFound(String appkey)
    {
        Class<? extends RemoteAppApplicationType> appTypeClass = applicationTypeClassLoader.getApplicationType(appkey);
        if (appTypeClass != null)
        {
            RemoteAppApplicationType type = typeAccessor.getApplicationType(appTypeClass);
            if (type != null)
            {
                return type;
            }
        }

        // not found
        return null;
    }

    public ApplicationLink getApplicationLink(String appkey) throws IllegalArgumentException
    {
        Class appTypeClass = applicationTypeClassLoader.getApplicationType(appkey);
        notNull(appTypeClass);
        ApplicationType type = typeAccessor.getApplicationType(appTypeClass);
        notNull(type);
        return getApplicationLink(type);
    }
}
