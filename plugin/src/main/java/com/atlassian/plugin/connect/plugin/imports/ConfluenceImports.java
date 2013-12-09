package com.atlassian.plugin.connect.plugin.imports;

import javax.inject.Inject;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.content.render.xhtml.StorageFormatCleaner;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.plugin.descriptor.web.ConfluenceWebFragmentHelper;
import com.atlassian.confluence.security.websudo.WebSudoManager;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.status.service.SystemInformationService;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.core.task.MultiQueueTaskManager;
import com.atlassian.plugin.spring.scanner.annotation.component.ClasspathComponent;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ConfluenceImport;

/**
 * This class does nothing but is here to centralize the Confluence component imports.
 * This is so we have a single place to put the annotations instead of scattering them around the entire project
 */
@SuppressWarnings ("ALL")
@ConfluenceComponent
public class ConfluenceImports
{
    private final ConfluenceWebFragmentHelper confluenceWebFragmentHelper;
    private final BandanaManager bandanaManager;
    private final I18NBeanFactory i18NBeanFactory;
    private final MultiQueueTaskManager multiQueueTaskManager;
    private final PageManager pageManager;
    private final SettingsManager settingsManager;
    private final SpaceManager spaceManager;
    private final StorageFormatCleaner storageFormatCleaner;
    private final SystemInformationService systemInformationService;
    private final WebSudoManager webSudoManager;
    private final XhtmlContent xhtmlContent;
    
    @Inject
    public ConfluenceImports(
            // this one's a component from an external jar, not an OSGi service
            @ConfluenceComponent ConfluenceWebFragmentHelper confluenceWebFragmentHelper,
            @ConfluenceImport BandanaManager bandanaManager,
            @ConfluenceImport I18NBeanFactory i18NBeanFactory,
            @ConfluenceImport MultiQueueTaskManager multiQueueTaskManager,
            @ConfluenceImport PageManager pageManager,
            @ConfluenceImport SettingsManager settingsManager,
            @ConfluenceImport SpaceManager spaceManager,
            @ConfluenceImport StorageFormatCleaner storageFormatCleaner,
            @ConfluenceImport SystemInformationService systemInformationService,
            @ConfluenceImport WebSudoManager webSudoManager,
            @ConfluenceImport XhtmlContent xhtmlContent
    )
    {
        this.confluenceWebFragmentHelper = confluenceWebFragmentHelper;
        this.bandanaManager = bandanaManager;
        this.i18NBeanFactory = i18NBeanFactory;
        this.multiQueueTaskManager = multiQueueTaskManager;
        this.pageManager = pageManager;
        this.settingsManager = settingsManager;
        this.spaceManager = spaceManager;
        this.storageFormatCleaner = storageFormatCleaner;
        this.systemInformationService = systemInformationService;
        this.webSudoManager = webSudoManager;
        this.xhtmlContent = xhtmlContent;
    }
}
