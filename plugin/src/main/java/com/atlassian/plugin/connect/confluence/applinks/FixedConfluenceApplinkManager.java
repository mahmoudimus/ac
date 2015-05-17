package com.atlassian.plugin.connect.confluence.applinks;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.persistence.dao.compatibility.FindUserHelper;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.OAuthLinkManager;
import com.atlassian.plugin.connect.plugin.applinks.ConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.applinks.DefaultConnectApplinkManager;
import com.atlassian.plugin.connect.plugin.applinks.NotConnectAddonException;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@ExportAsDevService
@ConfluenceComponent
public class FixedConfluenceApplinkManager extends DefaultConnectApplinkManager implements ConnectApplinkManager
{
    private static final Logger log = LoggerFactory.getLogger(FixedConfluenceApplinkManager.class);
    public static final String SYSADMIN = "sysadmin";

    @Inject
    public FixedConfluenceApplinkManager(MutatingApplicationLinkService applicationLinkService, TypeAccessor typeAccessor,
                                         PluginSettingsFactory pluginSettingsFactory, OAuthLinkManager oAuthLinkManager,
                                         TransactionTemplate transactionTemplate)
    {
        super(applicationLinkService, typeAccessor, pluginSettingsFactory, oAuthLinkManager, transactionTemplate);
    }

    @Override
    public void deleteAppLink(ConnectAddonBean addon) throws NotConnectAddonException
    {
        final String key = addon.getKey();
        final ApplicationLink link = getAppLink(key);

        deleteApplink(key, link);
    }

    private void deleteApplink(final String key, final ApplicationLink link)
    {
        if (link != null)
        {
            transactionTemplate.execute(new TransactionCallback<Void>()
            {
                @Override
                public Void doInTransaction()
                {
                    log.info("Removing application link for {}", key);

                    try
                    {
                        applicationLinkService.deleteApplicationLink(link);
                        return null;
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
                            applicationLinkService.deleteApplicationLink(link);

                            return null;
                        }
                        finally
                        {
                            AuthenticatedUserThreadLocal.set(originalUser);
                        }
                    }
                }

            } );
        }
        else
        {
            log.debug("Could not remove application link for {}", key);
        }
    }
}