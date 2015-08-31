package com.atlassian.plugin.connect.jira.capabilities.descriptor;

import com.atlassian.plugin.connect.spi.web.WebSectionLocationMetadataProvider;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableList;

import java.util.List;

@JiraComponent
public class JiraWebSectionLocationMetadataProvider implements WebSectionLocationMetadataProvider
{
    private final static List<String> MOVEABLE_SECTIONS = ImmutableList.of("jira.agile.board.tools");

    @Override
    public List<String> getMovableWebSectionLocations()
    {
        return MOVEABLE_SECTIONS;
    }
}
