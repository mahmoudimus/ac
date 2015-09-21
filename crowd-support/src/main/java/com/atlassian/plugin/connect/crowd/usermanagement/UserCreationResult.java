package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.embedded.api.User;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

public class UserCreationResult
{
    private User user;
    private boolean isNewlyCreated;

    public UserCreationResult(User user, UserNewness userNewness)
    {
        checkNotNull(user, "If a user was not created, something has gone wrong and an exception should be thrown");
        checkNotNull(userNewness, "The newness of the created user needs to be communicated");

        this.user = user;
        isNewlyCreated = (userNewness == UserNewness.NEWLY_CREATED);
    }

    @Nonnull
    public User getUser()
    {
        return user;
    }

    public boolean isNewlyCreated()
    {
        return isNewlyCreated;
    }

    public enum UserNewness {
        NEWLY_CREATED,
        PRE_EXISTING
    }
}
