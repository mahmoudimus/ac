package com.atlassian.plugin.connect.confluence;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.content.render.xhtml.StorageFormatCleaner;
import com.atlassian.confluence.core.ContentEntityManager;
import com.atlassian.confluence.license.LicenseService;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.security.SpacePermissionManager;
import com.atlassian.confluence.security.websudo.WebSudoManager;
import com.atlassian.confluence.setup.settings.CoreFeaturesManager;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.status.service.SystemInformationService;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.core.task.MultiQueueTaskManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.inject.Inject;

/**
 * This class does nothing but is here to centralize the Confluence component imports.
 * This is so we have a single place to put the annotations instead of scattering them around the entire project
 */
@SuppressWarnings ("ALL")
@Scanned
public class ConfluenceImports
{
    @Inject
    public ConfluenceImports(
            @ConfluenceImport BandanaManager bandanaManager,
            @ConfluenceImport @Qualifier("contentEntityManager") ContentEntityManager contentEntityManager,
            @ConfluenceImport I18NBeanFactory i18NBeanFactory,
            @ConfluenceImport LicenseService licenseService,
            @ConfluenceImport MultiQueueTaskManager multiQueueTaskManager,
            @ConfluenceImport PageManager pageManager,
            @ConfluenceImport ("confluencePermissionManager") PermissionManager permissionManager,
            @ConfluenceImport SettingsManager settingsManager,
            @ConfluenceImport SpaceManager spaceManager,
            @ConfluenceImport SpacePermissionManager spacePermissionManager,
            @ConfluenceImport StorageFormatCleaner storageFormatCleaner,
            @ConfluenceImport SystemInformationService systemInformationService,
            @ConfluenceImport UserAccessor userAccessor,
            @ConfluenceImport WebSudoManager webSudoManager,
            @ConfluenceImport XhtmlContent xhtmlContent,
            @ConfluenceImport CoreFeaturesManager coreFeaturesManager)
    {
    }
}
