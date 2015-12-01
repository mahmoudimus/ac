package at.confluence;

import java.rmi.RemoteException;

import com.atlassian.plugin.connect.test.common.at.pageobjects.ScopesTestPage;
import com.atlassian.plugin.connect.test.common.at.pageobjects.ScopesTestPage.Scope;
import com.atlassian.test.categories.OnDemandAcceptanceTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.marketplace.ConnectAddonRepresentation;
import at.marketplace.ExternalAddonInstaller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Installs a scope testing add-on maintained by the Atlassian Connect team,
 * triggers a test, then reads the results.
 *
 * The scope testing add-on repo is hosted on <a href=https://bitbucket.org/atlassianlabs/ac-acceptance-test-scope-checker>bitbucket</a>
 * but restricted to Atlassian developers. Please ensure changes are backwards compatible with other versions still in the deployment pipeline.
 */

// at.confluence.TestConfluenceScopes and at.jira.TestConfluenceScopes are very similar.
// If you make changes here, please check whether corresponding changes are needed in the other class.
// Issue for extracting shared functionality: ACDEV-2364
@Category (OnDemandAcceptanceTest.class)
public class TestConfluenceScopes extends ConfluenceAcceptanceTestBase
{
    private static final Logger log = LoggerFactory.getLogger(TestConfluenceScopes.class);
    public static final String SCOPE_TESTER_DESCRIPTOR_URL = "https://ac-acceptance-test-scope-checker.app.dev.atlassian.io/atlassian-connect.json";
    private ExternalAddonInstaller externalAddonInstaller;
    private ConnectAddonRepresentation addon;

    @Before
    public void installAddon() throws Exception
    {
        addon = ConnectAddonRepresentation.builder()
                .withDescriptorUrl(SCOPE_TESTER_DESCRIPTOR_URL)
                .withName("Atlassian Connect Scope Tester add-on")
                .withSummary("Tries to make calls for various scopes and then reports on the results")
                .withTagline("360 no scope")
                .build();

        externalAddonInstaller = new ExternalAddonInstaller(
                product.getProductInstance().getBaseUrl(),
                ADMIN,
                addon);

        log.info("Installing add-on in preparation for running a test in " + getClass().getName());
        externalAddonInstaller.install();
    }

    @Test
    public void testAdminScopeIsAuthorised() throws RemoteException
    {
        ScopesTestPage scopesTestPage = login(ADMIN, ScopesTestPage.class, externalAddonInstaller.getAddonKey());
        String adminResponseCode = scopesTestPage.getCodeForScope(Scope.ADMIN);
        assertThat("Admin-scoped request succeeded", adminResponseCode, is("200"));
    }

    @After
    public void uninstallAddon() throws Exception
    {
        log.info("Cleaning up after running a test in " + getClass().getName());
        externalAddonInstaller.uninstall();

    }

}
