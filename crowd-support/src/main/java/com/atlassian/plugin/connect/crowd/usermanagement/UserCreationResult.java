package com.atlassian.plugin.connect.crowd.usermanagement;

import com.atlassian.crowd.embedded.api.User;

public class UserCreationResult
{
    private User user;
    private boolean isNewlyCreated;

    public UserCreationResult(User user, boolean isNewlyCreated)
    {
        this.user = user;
        this.isNewlyCreated = isNewlyCreated;
    }

    public User getUser()
    {
        return user;
    }

    public boolean isNewlyCreated()
    {
        return isNewlyCreated;
    }
}
