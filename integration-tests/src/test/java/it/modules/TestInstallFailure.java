package it.modules;

import com.atlassian.fugue.Option;
import com.atlassian.jwt.core.reader.JwtIssuerSharedSecretService;
import com.atlassian.jwt.core.reader.JwtIssuerValidator;
import com.atlassian.jwt.core.reader.NimbusJwtReaderFactory;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtParseException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.atlassian.jwt.exception.JwtVerificationException;
import com.atlassian.jwt.reader.JwtClaimVerifier;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectPageModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.upm.pageobjects.PluginManager;
import com.google.common.collect.ImmutableMap;
import it.ConnectWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.InstallHandlerServlet;
import it.util.TestUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonAndModuleKey;
import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode.LINK_TEXT;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Tests that we clean up properly on plugin install failure, to avoid recurrence of AC-1187
 */
public class TestInstallFailure extends ConnectWebDriverTestBase
{

    private static final String MY_AWESOME_PAGE = "My Awesome Page";
    private static final String MY_AWESOME_PAGE_KEY = "my-awesome-page";
    private static final String URL = "/" + MY_AWESOME_PAGE_KEY;
    private static final CustomInstallationHandlerServlet installUninstallHandler = new CustomInstallationHandlerServlet();

    private static ConnectRunner remotePlugin;

    private static String sharedSecret;

    private String pluginKey;
    private String awesomePageModuleKey;

    @Before
    public void setup()
    {
        int query = URL.indexOf("?");
        String route = query > -1 ? URL.substring(0, query) : URL;

        ConnectPageModuleBeanBuilder pageBeanBuilder = newPageBean();
        pageBeanBuilder.withName(new I18nProperty(MY_AWESOME_PAGE, null))
                .withKey(MY_AWESOME_PAGE_KEY)
                .withUrl(URL)
                .withWeight(1234);
        
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
        .addInstallLifecycle()
        .addUninstallLifecycle()
        .addModule("configurePage", pageBeanBuilder.build())
        .addJWT()
        .addRoute(route, ConnectAppServlets.helloWorldServlet())
        .addRoute(ConnectRunner.INSTALLED_PATH, installUninstallHandler)
        .addRoute(ConnectRunner.UNINSTALLED_PATH, installUninstallHandler)
        .addScope(ScopeName.ADMIN)
        .disableInstallationStatusCheck();
    }

    public void installAddonSuccess() throws Exception
    {
        installUninstallHandler.setShouldSend404(false);
        remotePlugin.start();
        this.pluginKey = remotePlugin.getAddon().getKey();
        this.awesomePageModuleKey = addonAndModuleKey(pluginKey, MY_AWESOME_PAGE_KEY);
        sharedSecret = installUninstallHandler.getInstallPayload().getSharedSecret();
    }

    public void installAddonFailure() throws Exception
    {
        installUninstallHandler.setShouldSend404(true);
        remotePlugin.start();
    }

    @After
    public void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void testFailedFirstInstallDoesNotBreakRetries() throws Exception
    {
        installAddonFailure();
        installAddonSuccess();
        assertPageLinkWorks();
    }

    @Test
    public void testFailedUpgradeDoesNotUninstall() throws Exception
    {
        installAddonSuccess();
        installAddonFailure();
        assertPageLinkWorks();
    }

    public void assertPageLinkWorks() throws MalformedURLException, URISyntaxException, JwtVerificationException, JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, JwtParseException
    {
        login(TestUser.ADMIN);

        PluginManager page = product.visit(PluginManager.class);
        revealLinkIfNecessary(page);

        LinkedRemoteContent addonPage = connectPageOperations.findConnectPage(LINK_TEXT,
                "Configure",
                Option.<String>none(), awesomePageModuleKey);

        RemotePluginEmbeddedTestPage addonContentPage = addonPage.click();

        assertThat(addonContentPage.isLoaded(), equalTo(true));

        ConnectAsserts.verifyContainsStandardAddOnQueryParamters(addonContentPage.getIframeQueryParams(),
                product.getProductInstance().getContextPath());


        final String jwt = addonContentPage.getIframeQueryParams().get("jwt");
        assertNotNull(jwt);

        final JwtIssuerSharedSecretService sharedSecretService = new JwtIssuerSharedSecretService()
        {

            @Override
            public String getSharedSecret(String issuer) throws JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException
            {
                return sharedSecret;
            }
        };

        final JwtIssuerValidator jwtIssuerValidator = new JwtIssuerValidator()
        {

            @Override
            public boolean isValid(String issuer)
            {
                return true;
            }
        };

        final NimbusJwtReaderFactory readerFactory = new NimbusJwtReaderFactory(jwtIssuerValidator, sharedSecretService);

        // this will fail with JwtSignatureMismatchException if we have a problem with cleaning up app links on
        // addon install failure.
        // Can't think of a meaningful assertion for this test as it is just the absence of an exception that indicates
        // success
        readerFactory.getReader(jwt).read(jwt, ImmutableMap.<String, JwtClaimVerifier>of());
    }

    private <T extends Page> void revealLinkIfNecessary(T page)
    {
        // hmmm not pretty
        ((PluginManager) page).expandPluginRow(pluginKey);
    }

    private static class CustomInstallationHandlerServlet extends HttpServlet
    {
        private boolean shouldSend404 = true;

        InstallHandlerServlet installHandlerServlet = new InstallHandlerServlet();

        protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException
        {
            installHandlerServlet.service(req, resp);
            if (shouldSend404)
            {
                resp.sendError(404);
            }
        }

        public void setShouldSend404(boolean shouldSend404)
        {
            this.shouldSend404 = shouldSend404;
        }

        public InstallHandlerServlet.InstallPayload getInstallPayload()
        {
            return installHandlerServlet.getInstallPayload();
        }
    }
}


