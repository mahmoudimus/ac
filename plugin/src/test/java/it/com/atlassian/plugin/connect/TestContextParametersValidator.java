package it.com.atlassian.plugin.connect;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.connect.spi.module.ContextParametersValidator;
import com.atlassian.plugin.connect.spi.module.PermissionCheck;
import com.atlassian.plugin.connect.spi.module.PermissionChecks;
import com.google.common.collect.ImmutableList;

import java.util.Collection;

public final class TestContextParametersValidator implements ContextParametersValidator<ApplicationUser>
{
    @Override
    public Collection<PermissionCheck<ApplicationUser>> getPermissionChecks()
    {
        return ImmutableList.of(
                PermissionChecks.<ApplicationUser>mustBeLoggedIn("project.keyConcatId")
        );
    }

    @Override
    public Class<ApplicationUser> getUserType()
    {
        return ApplicationUser.class;
    }
}
