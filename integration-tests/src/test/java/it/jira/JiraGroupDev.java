package it.jira;

import it.AbstractGroupClient;

/**
 * Only runs confluence tests, used for development
 */
public class JiraGroupDev extends AbstractGroupClient
{
    public JiraGroupDev()
    {
        super("jira", 2990, "/jira");
    }
}
