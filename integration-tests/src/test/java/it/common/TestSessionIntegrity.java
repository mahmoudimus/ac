package it.common;

import com.atlassian.jwt.core.writer.NimbusJwtWriterFactory;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import it.servlet.ConnectAppServlets;
import it.servlet.InstallHandlerServlet;
import it.util.ConnectTestUserFactory;
import it.util.JwtAuthorizationGenerator;
import it.util.TestUser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestSessionIntegrity extends MultiProductWebDriverTestBase
{
    private static final String HTTP_GET = "GET";

    private static ConnectRunner runner;
    private static InstallHandlerServlet installHandler;

    private final JwtAuthorizationGenerator jwtAuthorizationGenerator = new JwtAuthorizationGenerator(new NimbusJwtWriterFactory());

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        installHandler = ConnectAppServlets.installHandlerServlet();
        runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
                .addModule("generalPages", newPageBean()
                        .withKey("page")
                        .withName(new I18nProperty("Page", null))
                        .withUrl("/page")
                        .build())
                .addJWT(installHandler)
                .addScope(ScopeName.READ)
                .addRoute("/page", ConnectAppServlets.helloWorldServlet())
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (runner != null)
        {
            runner.stopAndUninstall();
        }
    }

    @Test
    public void addOnUserDoesNotInheritTheSession() throws Exception
    {
        login(ConnectTestUserFactory.sysadmin(product));

        InstallHandlerServlet.InstallPayload installPayload = installHandler.getInstallPayload();

        String baseUrl = product.getProductInstance().getBaseUrl();
        URI uri = URI.create(baseUrl + "/rest/atlassian-connect/latest/license");
        String jwtToken = jwtAuthorizationGenerator.generate(HTTP_GET, product.getProductInstance().getContextPath(),
                uri, Maps.<String, List<String>>newHashMap(), Optional.<String>absent(),
                runner.getAddon().getKey(), installPayload.getSharedSecret());
        String signedUrl = uri.toString() + "?jwt=" + jwtToken;

        // First assert (through the backdoor) that the JWT token validates.
        // This is to avoid a successful test result if the request didn't pass the JwtFilter for some reason.
        // Could be avoided if webdriver allowed to check response status codes...
        URL url = new URL(signedUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(HTTP_GET);

        // This resource only works with JWT (which makes it a good candidate for this test)
        // It returns a 404 if the call succeeded but no license is present
        // It returns a 401 if JWT auth failed
        // It returns 400 if no JWT token was provided
        // The main assertion is that there is no 401, i.e. the call went through the JWT auth filter
        assertEquals(404, connection.getResponseCode());

        // Now hit the same URL within the browser session
        WebDriver driver = product.getTester().getDriver().getDriver();
        driver.get(signedUrl);

        // Then get the current session user
        driver.get(baseUrl + "/rest/remoteplugintest/1/user");

        // We destroy the session, so 'anonymous' is expected, not the add-on user
        assertTrue(driver.getPageSource().contains("<user><name>anonymous</name></user>"));
    }

}
