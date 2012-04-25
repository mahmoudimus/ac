package com.atlassian.labs.remoteapps;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.labs.remoteapps.modules.applinks.ApplicationTypeClassLoader;
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
        notNull(link);
        return link;
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
