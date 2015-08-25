package com.atlassian.plugin.connect.jira.capabilities.descriptor;

import com.atlassian.plugin.connect.spi.web.MovableWebSectionKeysProvider;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableList;

import java.util.List;

@JiraComponent
public class JiraMovableWebSectionKeysProvider implements MovableWebSectionKeysProvider
{
    private final static List<String> MOVEABLE_SECTIONS = ImmutableList.of("jira.agile.board.tools");

    @Override
    public List<String> provide()
    {
        return MOVEABLE_SECTIONS;
    }
}
