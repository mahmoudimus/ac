package it.util;

public class TestUser
{
    public static final TestUser ADMIN = new TestUser("admin", "admin_s3cr7", "Administrator", "admin@example.com");
    public static final TestUser BARNEY = new TestUser("barney", "i<3betty", "Administrator", "admin@example.com");
    public static final TestUser BETTY = new TestUser("betty", "(foreveralone)", "Administrator", "admin@example.com");

    private final String username;
    private final String password;
    private final String displayName;
    private final String email;

    public TestUser(final String username, final String password, final String displayName, final String email)
    {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.email = email;
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
