package com.atlassian.plugin.connect.plugin.imports;

import com.atlassian.crowd.manager.application.AbstractDelegatingApplicationService;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.component.ComponentLocator;

@ConfluenceComponent
@ExportAsDevService
public class ConfluenceApplicationService extends AbstractDelegatingApplicationService implements ApplicationService
{
    public ConfluenceApplicationService()
    {
        super(ComponentLocator.getComponent(ApplicationService.class));
    }
}
