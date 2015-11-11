package com.atlassian.plugin.connect.jira.web.panel;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JiraWebPanelLocationValidatorTest
{

    private final JiraWebPanelLocationValidator webPanelLocationValidator = new JiraWebPanelLocationValidator();

    @Test
    public void testAtlHeaderAfterScriptsIsBlacklisted()
    {
        assertFalse(webPanelLocationValidator.validateLocation("atl.header.after.scripts"));
    }

    @Test
    public void testProjectCentricNavigationWebPanel()
    {
        assertTrue(webPanelLocationValidator.validateLocation("com.atlassian.jira.jira-projects-plugin:release-page"));
    }
}
