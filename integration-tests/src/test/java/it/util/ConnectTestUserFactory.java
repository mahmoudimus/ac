package it.util;

import com.atlassian.plugin.connect.test.helptips.HelpTipApiClient;
import org.apache.commons.lang.RandomStringUtils;

public abstract class ConnectTestUserFactory
{
    public TestUser admin()
    {
        return createAndInitializeTestUser(AuthLevel.ADMIN);
    }

    public TestUser basicUser()
    {
        return createAndInitializeTestUser(AuthLevel.BASIC_USER);
    }

    protected abstract TestUser createTestUser(AuthLevel authLevel, String username);

    protected abstract HelpTipApiClient getHelpTipApiClient(TestUser testUser);

    private TestUser createAndInitializeTestUser(AuthLevel authLevel)
    {
        String username = authLevel.getPrefix() + "-" + RandomStringUtils.randomAlphanumeric(20).toLowerCase();
        TestUser testUser = createTestUser(authLevel, username);
        disableHelpTips(testUser);
        return testUser;
    }

    private void disableHelpTips(TestUser testUser)
    {
        try
        {
            getHelpTipApiClient(testUser).dismissAllHelpTips();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected enum AuthLevel
    {
        ADMIN("test-admin"),
        BASIC_USER("test-user");

        private String prefix;

        AuthLevel(String prefix)
        {
            this.prefix = prefix;
        }

        public String getPrefix()
        {
            return prefix;
        }
    }
}
