package com.atlassian.plugin.connect.plugin.imports;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.content.render.xhtml.StorageFormatCleaner;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.plugin.descriptor.web.ConfluenceWebFragmentHelper;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.security.websudo.WebSudoManager;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.status.service.SystemInformationService;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.core.task.MultiQueueTaskManager;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;

import javax.inject.Inject;

/**
 * This class does nothing but is here to centralize the Confluence component imports.
 * This is so we have a single place to put the annotations instead of scattering them around the entire project
 */
@SuppressWarnings ("ALL")
@ConfluenceComponent
public class ConfluenceImports
{
    @Inject
    public ConfluenceImports(
            // this one's a component from an external jar, not an OSGi service
            @ConfluenceComponent ConfluenceWebFragmentHelper confluenceWebFragmentHelper,
            @ConfluenceImport BandanaManager bandanaManager,
            @ConfluenceImport I18NBeanFactory i18NBeanFactory,
            @ConfluenceImport MultiQueueTaskManager multiQueueTaskManager,
            @ConfluenceImport PageManager pageManager,
            @ConfluenceImport ("confluencePermissionManager") PermissionManager permissionManager,
            @ConfluenceImport SettingsManager settingsManager,
            @ConfluenceImport SpaceManager spaceManager,
            @ConfluenceImport StorageFormatCleaner storageFormatCleaner,
            @ConfluenceImport SystemInformationService systemInformationService,
            @ConfluenceImport WebSudoManager webSudoManager,
            @ConfluenceImport UserAccessor userAccessor,
            @ConfluenceImport XhtmlContent xhtmlContent
    )
    {
    }
}
