package it.util;

import com.atlassian.confluence.it.User;
import com.atlassian.confluence.it.rpc.ConfluenceRpc;
import com.atlassian.confluence.pageobjects.ConfluenceTestedProduct;
import com.atlassian.plugin.connect.test.helptips.ConfluenceHelpTipApiClient;
import com.atlassian.plugin.connect.test.helptips.HelpTipApiClient;

public class ConfluenceTestUserFactory extends ConnectTestUserFactory
{

    private final ConfluenceTestedProduct product;

    private final ConfluenceRpc rpc;

    public ConfluenceTestUserFactory(ConfluenceTestedProduct product)
    {
        this(product, ConfluenceRpc.newInstance(product.getProductInstance().getBaseUrl()));
    }

    public ConfluenceTestUserFactory(ConfluenceTestedProduct product, ConfluenceRpc rpc)
    {
        this.product = product;
        this.rpc = rpc;
    }

    @Override
    protected TestUser createTestUser(AuthLevel authLevel, String username)
    {
        TestUser testUser = new TestUser(username);
        rpc.useAnonymously();
        rpc.createUser(new User(username, username, username, username + "@example.com"));
        addConfluencePermissionsForTestUser(testUser, authLevel);
        return testUser;
    }

    @Override
    protected HelpTipApiClient getHelpTipApiClient(TestUser testUser)
    {
        return new ConfluenceHelpTipApiClient(product, testUser);
    }

    private void addConfluencePermissionsForTestUser(TestUser testUser, AuthLevel authLevel)
    {
        switch (authLevel)
        {
            case ADMIN:
                rpc.addUserToGroup(testUser.getUsername(), "confluence-administrators");
            case BASIC_USER:
                rpc.addUserToGroup(testUser.getUsername(), "confluence-users");
        }
    }
}
