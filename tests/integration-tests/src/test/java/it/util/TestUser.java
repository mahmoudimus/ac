package it.util;

import com.atlassian.jira.tests.TestBase;

public class TestUser
{
    private final String username;
    private final String password;
    private final String displayName;
    private final String email;
    
    public TestUser(final String username)
    {
        this.username = username;
        this.password = username;
        this.displayName = username;
        this.email = username + "@example.com";
        
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getEmail()
    {
        return email;
    }

    public com.atlassian.confluence.it.User confUser()
    {
        return new com.atlassian.confluence.it.User(username, password, displayName, email);
    }
}
