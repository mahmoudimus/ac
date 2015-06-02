package com.atlassian.plugin.connect.confluence.applinks;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationException;
import com.atlassian.applinks.spi.auth.AuthenticationScenario;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.MutableApplicationLink;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.link.ReciprocalActionException;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.persistence.dao.compatibility.FindUserHelper;
import com.atlassian.plugin.connect.spi.applinks.MutatingApplicationLinkServiceProvider;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.net.ResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import javax.inject.Inject;

@ConfluenceComponent
public class ConfluenceApplinkServiceProvider implements MutatingApplicationLinkServiceProvider
{
    private static final Logger log = LoggerFactory.getLogger(ConfluenceApplinkServiceProvider.class);
    public static final String SYSADMIN = "sysadmin";

    private MutatingApplicationLinkService confluenceApplicationLinkService;

    @Inject
    public ConfluenceApplinkServiceProvider(MutatingApplicationLinkService applicationLinkService)
    {
        this.confluenceApplicationLinkService = new ConfluenceApplicationLinkServiceDecorator(applicationLinkService);
    }

    @Override
    public MutatingApplicationLinkService getMutatingApplicationLinkService()
    {
        return confluenceApplicationLinkService;
    }

    private class ConfluenceApplicationLinkServiceDecorator implements MutatingApplicationLinkService
    {
        private MutatingApplicationLinkService linkService;

        public ConfluenceApplicationLinkServiceDecorator(final MutatingApplicationLinkService linkService)
        {
            this.linkService = linkService;
        }

        @Override
        public MutableApplicationLink addApplicationLink(final ApplicationId applicationId, final ApplicationType applicationType, final ApplicationLinkDetails applicationLinkDetails)
        {
            return linkService.addApplicationLink(applicationId, applicationType, applicationLinkDetails);
        }

        @Override
        public void deleteReciprocatedApplicationLink(final ApplicationLink applicationLink)
                throws ReciprocalActionException, CredentialsRequiredException
        {
            linkService.deleteReciprocatedApplicationLink(applicationLink);
        }

        @Override
        public void deleteApplicationLink(final ApplicationLink applicationLink)
        {
            try
            {
                linkService.deleteApplicationLink(applicationLink);
            }
            catch (IllegalArgumentException e)
            {
                log.debug("retrying deleteApplicationLink as sysadmin");
                //try again as sysadmin
                ConfluenceUser originalUser = null;
                try
                {
                    originalUser = AuthenticatedUserThreadLocal.get();

                    ConfluenceUser user = FindUserHelper.getUserByUsername(SYSADMIN);
                    AuthenticatedUserThreadLocal.set(user);
                    linkService.deleteApplicationLink(applicationLink);
                }
                finally
                {
                    AuthenticatedUserThreadLocal.set(originalUser);
                }
            }
        }

        @Override
        public MutableApplicationLink getApplicationLink(final ApplicationId applicationId)
                throws TypeNotInstalledException
        {
            return linkService.getApplicationLink(applicationId);
        }

        @Override
        public Iterable<ApplicationLink> getApplicationLinks()
        {
            return linkService.getApplicationLinks();
        }

        @Override
        public Iterable<ApplicationLink> getApplicationLinks(final Class<? extends ApplicationType> aClass)
        {
            return linkService.getApplicationLinks(aClass);
        }

        @Override
        public ApplicationLink getPrimaryApplicationLink(final Class<? extends ApplicationType> aClass)
        {
            return linkService.getPrimaryApplicationLink(aClass);
        }

        @Override
        public void makePrimary(final ApplicationId applicationId) throws TypeNotInstalledException
        {
            linkService.makePrimary(applicationId);
        }

        @Override
        public void setSystem(final ApplicationId applicationId, final boolean b) throws TypeNotInstalledException
        {
            linkService.setSystem(applicationId, b);
        }

        @Override
        public void changeApplicationId(final ApplicationId applicationId, final ApplicationId applicationId1)
                throws TypeNotInstalledException
        {
            linkService.changeApplicationId(applicationId, applicationId1);
        }

        @Override
        public ApplicationLink createApplicationLink(final ApplicationType applicationType, final ApplicationLinkDetails applicationLinkDetails)
                throws ManifestNotFoundException
        {
            return linkService.createApplicationLink(applicationType, applicationLinkDetails);
        }

        @Override
        public void createReciprocalLink(final URI uri, final URI uri1, final String s, final String s1)
                throws ReciprocalActionException
        {
            linkService.createReciprocalLink(uri, uri1, s, s1);
        }

        @Override
        public boolean isAdminUserInRemoteApplication(final URI uri, final String s, final String s1)
                throws ResponseException
        {
            return linkService.isAdminUserInRemoteApplication(uri, s, s1);
        }

        @Override
        public void configureAuthenticationForApplicationLink(final ApplicationLink applicationLink, final AuthenticationScenario authenticationScenario, final String s, final String s1)
                throws AuthenticationConfigurationException
        {
            linkService.configureAuthenticationForApplicationLink(applicationLink, authenticationScenario, s, s1);
        }

        @Override
        public URI createSelfLinkFor(final ApplicationId applicationId)
        {
            return linkService.createSelfLinkFor(applicationId);
        }

        @Override
        public boolean isNameInUse(final String s, final ApplicationId applicationId)
        {
            return linkService.isNameInUse(s, applicationId);
        }
    }
}
