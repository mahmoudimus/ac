package com.atlassian.plugin.connect.testsupport.context.params;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.connect.spi.module.ContextParametersValidator;
import com.atlassian.plugin.connect.spi.module.PermissionCheck;
import com.atlassian.plugin.connect.spi.module.PermissionChecks;

import java.util.Collection;
import java.util.Collections;

public class TestParametersValidator implements ContextParametersValidator<ApplicationUser>
{
    @Override
    public Collection<PermissionCheck<ApplicationUser>> getPermissionChecks()
    {
        return Collections.singleton(PermissionChecks.alwaysAllowed("testParam"));
    }

    @Override
    public Class<ApplicationUser> getUserType()
    {
        return ApplicationUser.class;
    }
}
