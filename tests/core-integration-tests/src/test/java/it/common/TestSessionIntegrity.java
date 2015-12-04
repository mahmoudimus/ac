package it.common;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import com.atlassian.jwt.core.writer.NimbusJwtWriterFactory;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.InstallHandlerServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.google.common.collect.Maps;
import it.util.JwtAuthorizationGenerator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

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
        runner = new ConnectRunner(baseUrl(), AddonTestUtils.randomAddOnKey())
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
        WebDriver driver = product.getTester().getDriver().getDriver();

        login(testUserFactory.basicUser());

        driver.get(signWithJwt("/rest/remoteplugintest/1/user"));
        assertThat(driver.getPageSource(), containsString(user("addon_" + runner.getAddon().getName())));

        driver.get(unsigned(""));
        assertThat(driver.getPageSource(), containsString("Login"));
    }

    private String signWithJwt(String url) throws JwtUnknownIssuerException, URISyntaxException, JwtIssuerLacksSharedSecretException
    {
        InstallHandlerServlet.InstallPayload installPayload = installHandler.getInstallPayload();
        String baseUrl = baseUrl();
        URI uri = URI.create(baseUrl + url);
        String jwtToken = jwtAuthorizationGenerator.generate(HTTP_GET, product.getProductInstance().getContextPath(),
                uri, Maps.<String, List<String>>newHashMap(), Optional.<String>empty(),
                runner.getAddon().getKey(), installPayload.getSharedSecret());
        return uri.toString() + "?jwt=" + jwtToken;
    }

    private String unsigned(String url)
    {
        return baseUrl() + url;
    }

    private String user(String name)
    {
        return "<user><name>" + name + "</name></user>";
    }

    private static String baseUrl()
    {
        return product.getProductInstance().getBaseUrl();
    }
}
