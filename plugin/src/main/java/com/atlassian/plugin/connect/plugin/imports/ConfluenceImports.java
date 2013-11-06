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
@ConfluenceComponent
public class ConfluenceImports
{
    private final ConfluenceWebFragmentHelper confluenceWebFragmentHelper;
    private final SettingsManager settingsManager;
    private final BandanaManager bandanaManager;
    private final SpaceManager spaceManager;
    private final PageManager pageManager;
    private final SystemInformationService systemInformationService;
    private final XhtmlContent xhtmlContent;
    private final I18NBeanFactory i18NBeanFactory;
    private final WebSudoManager webSudoManager;
    private final MultiQueueTaskManager multiQueueTaskManager;
    private final StorageFormatCleaner storageFormatCleaner;
    
    @Inject
    public ConfluenceImports(
            // this one's a component from an external jar, not an OSGi service
            @ConfluenceComponent ConfluenceWebFragmentHelper confluenceWebFragmentHelper,
            @ConfluenceImport SettingsManager settingsManager,
            @ConfluenceImport BandanaManager bandanaManager,
            @ConfluenceImport SpaceManager spaceManager,
            @ConfluenceImport PageManager pageManager,
            @ConfluenceImport SystemInformationService systemInformationService,
            @ConfluenceImport XhtmlContent xhtmlContent,
            @ConfluenceImport I18NBeanFactory i18NBeanFactory,
            @ConfluenceImport WebSudoManager webSudoManager,
            @ConfluenceImport MultiQueueTaskManager multiQueueTaskManager,
            @ConfluenceImport StorageFormatCleaner storageFormatCleaner)
    {
        this.confluenceWebFragmentHelper = confluenceWebFragmentHelper;
        this.settingsManager = settingsManager;
        this.bandanaManager = bandanaManager;
        this.spaceManager = spaceManager;
        this.pageManager = pageManager;
        this.systemInformationService = systemInformationService;
        this.xhtmlContent = xhtmlContent;
        this.i18NBeanFactory = i18NBeanFactory;
        this.webSudoManager = webSudoManager;
        this.multiQueueTaskManager = multiQueueTaskManager;
        this.storageFormatCleaner = storageFormatCleaner;
    }
}
