package com.atlassian.plugin.connect;

import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugin.connect.spi.module.ContextParametersValidator;
import com.atlassian.plugin.connect.spi.module.PermissionCheck;
import com.atlassian.plugin.connect.spi.module.PermissionChecks;
import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public final class TestConfluenceContextParametersValidator implements ContextParametersValidator<ConfluenceUser>
{
    @Override
    public Collection<PermissionCheck<ConfluenceUser>> getPermissionChecks()
    {
        return ImmutableList.of(
                PermissionChecks.<ConfluenceUser>mustBeLoggedIn("space.keyConcatId")
        );
    }

    @Override
    public Class<ConfluenceUser> getUserType()
    {
        return ConfluenceUser.class;
    }
}
