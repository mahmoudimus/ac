package com.atlassian.plugin.connect.jira.field.option;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.sal.api.user.UserProfile;

public interface AuthenticationData
{
    interface AuthenticationDetailsVisitor<T>
    {
        T visit(AuthenticationData.Request authenticationBy);

        T visit(AuthenticationData.AddonKey authenticationBy);

        T visit(AuthenticationData.User authenticationBy);
    }

    <T> T accept(AuthenticationDetailsVisitor<T> visitor);

    interface Request extends AuthenticationData
    {
        HttpServletRequest getRequest();
    }

    interface AddonKey extends AuthenticationData
    {
        String getAddonKey();
    }

    interface User extends AuthenticationData
    {
        UserProfile getUser();
    }

    static AuthenticationData.Request byRequest(HttpServletRequest request)
    {
        return new AuthenticationData.Request()
        {
            @Override
            public HttpServletRequest getRequest()
            {
                return request;
            }

            @Override
            public <T> T accept(final AuthenticationDetailsVisitor<T> visitor)
            {
                return visitor.visit(this);
            }
        };
    }

    static AuthenticationData.User byUser(UserProfile userProfile)
    {
        return new AuthenticationData.User()
        {
            @Override
            public UserProfile getUser()
            {
                return userProfile;
            }

            @Override
            public <T> T accept(final AuthenticationDetailsVisitor<T> visitor)
            {
                return visitor.visit(this);
            }
        };
    }

    static AuthenticationData.AddonKey byAddonKey(String addonKey)
    {
        return new AuthenticationData.AddonKey()
        {
            @Override
            public String getAddonKey()
            {
                return addonKey;
            }

            @Override
            public <T> T accept(final AuthenticationDetailsVisitor<T> visitor)
            {
                return visitor.visit(this);
            }
        };
    }
}
