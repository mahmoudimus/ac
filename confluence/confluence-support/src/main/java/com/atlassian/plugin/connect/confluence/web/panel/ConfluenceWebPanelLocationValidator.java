package com.atlassian.plugin.connect.confluence.web.panel;

import com.atlassian.plugin.connect.spi.web.panel.WebPanelLocationValidator;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

@ConfluenceComponent
public class ConfluenceWebPanelLocationValidator implements WebPanelLocationValidator
{
    @Override
    public boolean validateLocation(String location)
    {
        return true;
    }
}
