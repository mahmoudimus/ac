package it;

public class TestConstants
{
    public static final String ADMIN_FULL_NAME = "A. D. Ministrator (Sysadmin)";
    public static final String ADMIN_USERNAME = "admin";
    public static final String BETTY_USERNAME = "betty";
    public static final String BARNEY_USERNAME = "barney";
    public static final String ANONYMOUS = "anonymous";

    public static final TestUser ADMIN = new TestUser(ADMIN_USERNAME, ADMIN_USERNAME, "Administrator", "admin@example.com");
    public static final TestUser BARNEY = new TestUser(BARNEY_USERNAME, BARNEY_USERNAME, "Administrator", "admin@example.com");
    public static final TestUser BETTY = new TestUser(BETTY_USERNAME, BETTY_USERNAME, "Administrator", "admin@example.com");

    public static class TestUser
    {
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

        public com.atlassian.confluence.it.User confTestUser()
        {
            return new com.atlassian.confluence.it.User(username, password, displayName, email);
        }
    }
}
