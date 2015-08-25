package com.atlassian.plugin.connect.confluence.capabilities.descriptor;

import com.atlassian.plugin.connect.spi.web.MovableWebSectionKeysProvider;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import java.util.Collections;
import java.util.List;

@ConfluenceComponent
public class ConfluenceMovableWebSectionKeysProvider implements MovableWebSectionKeysProvider
{
    @Override
    public List<String> provide()
    {
        return Collections.emptyList();
    }
}
