package com.atlassian.plugin.connect.jira.web.panel;

import com.atlassian.plugin.connect.spi.web.panel.WebPanelLocationValidator;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableList;

import java.util.List;

@JiraComponent
public class JiraWebPanelLocationValidator implements WebPanelLocationValidator
{
    private static final List<String> BLACKLIST = ImmutableList.of(
            // This location is blacklisted because of possible phishing, if a web-panel is put there, see ACJIRA-647
            "atl.header.after.scripts"
    );

    @Override
    public boolean validateLocation(String location) {
        return !BLACKLIST.contains(location);
    }
}
