package it.common.lifecycle;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.jwt.core.reader.JwtIssuerSharedSecretService;
import com.atlassian.jwt.core.reader.JwtIssuerValidator;
import com.atlassian.jwt.core.reader.NimbusJwtReaderFactory;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtParseException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.atlassian.jwt.exception.JwtVerificationException;
import com.atlassian.jwt.reader.JwtClaimVerifier;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.common.matcher.ConnectAsserts;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonEmbeddedTestPage;
import com.atlassian.plugin.connect.test.common.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.InstallHandlerServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.upm.pageobjects.PluginManager;

import com.google.common.collect.ImmutableMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.common.MultiProductWebDriverTestBase;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests that we clean up properly on plugin install failure, to avoid recurrence of AC-1187
 */
public class TestInstallFailure extends MultiProductWebDriverTestBase {

    protected static final String MY_AWESOME_PAGE = "My Awesome Page";
    protected static final String MY_AWESOME_PAGE_KEY = "my-awesome-page";
    protected static final String URL = "/" + MY_AWESOME_PAGE_KEY;
    protected static final CustomInstallationHandlerServlet installUninstallHandler = new CustomInstallationHandlerServlet();
    protected static ConnectRunner remotePlugin;

    private String sharedSecret;

    private String pluginKey;
    protected String awesomePageModuleKey;

    @Before
    public void setup() throws NoSuchAlgorithmException, IOException {
        int query = URL.indexOf("?");
        String route = query > -1 ? URL.substring(0, query) : URL;

        ConnectPageModuleBean pageBean = newPageBean()
                .withName(new I18nProperty(MY_AWESOME_PAGE, null))
                .withKey(MY_AWESOME_PAGE_KEY)
                .withUrl(URL)
                .withLocation(getGloballyVisibleLocation()).build();

        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddonKey())
                .addUninstallLifecycle()
                .addModules("generalPages", pageBean)
                .addJWT(installUninstallHandler)
                .addRoute(route, ConnectAppServlets.helloWorldServlet())
                .addRoute(ConnectRunner.UNINSTALLED_PATH, installUninstallHandler)
                .addScope(ScopeName.ADMIN)
                .disableInstallationStatusCheck();
    }

    public void installAddonSuccess() throws Exception {
        this.sharedSecret = installAddonSuccessAndReturnSecret();
    }

    public String installAddonSuccessAndReturnSecret() throws Exception {
        installUninstallHandler.setShouldSend404(false);
        remotePlugin.start();
        this.pluginKey = remotePlugin.getAddon().getKey();
        this.awesomePageModuleKey = addonAndModuleKey(pluginKey, MY_AWESOME_PAGE_KEY);
        return installUninstallHandler.getInstallPayload().getSharedSecret();
    }

    public void installAddonFailure() throws Exception {
        installUninstallHandler.setShouldSend404(true);
        remotePlugin.start();
    }

    @After
    public void stopConnectAddon() throws Exception {
        if (remotePlugin != null) {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void testFailedFirstInstallDoesNotBreakRetries() throws Exception {
        installAddonFailure();
        installAddonSuccess();
        assertPageLinkWorks();
    }

    @Test
    public void testFailedUpgradeDoesNotUninstall() throws Exception {
        installAddonSuccess();
        installAddonFailure();
        assertPageLinkWorks();
    }

    @Test
    //See https://ecosystem.atlassian.net/browse/ACDEV-1174
    public void testMultipleInstallsDoNotChangeSharedSecret() throws Exception {
        String firstSecret = installAddonSuccessAndReturnSecret();
        String secondSecret = installAddonSuccessAndReturnSecret();
        assertEquals(firstSecret, secondSecret);
    }

    @Test
    public void testFailedInstallDoesNotInstall() throws Exception {
        installAddonFailure();
        assertAddonIsNotInstalled();
    }

    public void assertAddonIsNotInstalled() {
        login(testUserFactory.admin());
        PluginManager page = product.visit(PluginManager.class);
        assertTrue("Plugin '" + pluginKey + "' should not be installed", !page.contains(pluginKey));
    }

    public void assertPageLinkWorks() throws MalformedURLException, URISyntaxException, JwtVerificationException, JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, JwtParseException {
        loginAndVisit(testUserFactory.basicUser(), HomePage.class);

        GeneralPage page = product.getPageBinder().bind(GeneralPage.class, MY_AWESOME_PAGE_KEY, remotePlugin.getAddon().getKey());
        ConnectAddonEmbeddedTestPage addonContentPage = page.clickAddonLink();

        ConnectAsserts.verifyContainsStandardAddonQueryParameters(addonContentPage.getIframeQueryParams(),
                product.getProductInstance().getContextPath());

        final String jwt = addonContentPage.getIframeQueryParams().get("jwt");
        assertNotNull(jwt);
        assertValidJwt(jwt);
    }

    private void assertValidJwt(String jwt) throws JwtParseException, JwtVerificationException, JwtUnknownIssuerException, JwtIssuerLacksSharedSecretException {
        final JwtIssuerSharedSecretService sharedSecretService = issuer -> sharedSecret;

        final JwtIssuerValidator jwtIssuerValidator = issuer -> true;

        final NimbusJwtReaderFactory readerFactory = new NimbusJwtReaderFactory(jwtIssuerValidator, sharedSecretService);

        // this will fail with JwtSignatureMismatchException if we have a problem with cleaning up app links on
        // addon install failure.
        // Can't think of a meaningful assertion for this test as it is just the absence of an exception that indicates
        // success
        readerFactory.getReader(jwt).read(jwt, ImmutableMap.<String, JwtClaimVerifier>of());
    }

    private static class CustomInstallationHandlerServlet extends HttpServlet {
        private boolean shouldSend404 = true;

        InstallHandlerServlet installHandlerServlet = new InstallHandlerServlet();

        protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
            installHandlerServlet.service(req, resp);
            if (shouldSend404) {
                resp.sendError(404);
            }
        }

        public void setShouldSend404(boolean shouldSend404) {
            this.shouldSend404 = shouldSend404;
        }

        public InstallHandlerServlet.InstallPayload getInstallPayload() {
            return installHandlerServlet.getInstallPayload();
        }
    }
}


