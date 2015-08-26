package com.atlassian.plugin.connect.jira.usermanagement;

import java.util.Set;

import com.atlassian.jira.license.LicenseChangedEvent;
import com.atlassian.jira.license.LicenseDetails;

import static org.mockito.Mockito.mock;

public class TestJiraLicenseChangeListener
{
    // One or both of the license details are empty

    // There are both new and existing applications
    // * It only updates the add-on user for the new ones
    // * It asks for the add-on users the right way
    // * It asks for the groups the right way
    // * It adds the add-on users to the groups

    // ensureUserIsInGroup throws for one of the users
    // * The rest of the users still get added

    void mockLicenseKeys(LicenseChangedEvent event, Set<String> oldKeys, Set<String> newKeys)
    {
        LicenseDetails oldLicenseDetails = mock(LicenseDetails.class);
        LicenseDetails newLicenseDetails = mock(LicenseDetails.class);
    }
}