package it.common.rest;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.rest.license.LicenseDetailsRepresentation;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.LicenseUtils;
import com.atlassian.plugin.connect.test.helptips.HelpTipApiClient;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.gson.Gson;
import it.ConnectWebDriverTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.InstallHandlerServlet;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URI;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @since 1.0
 */
public class TestLicenseRestResource extends ConnectWebDriverTestBase
{
    @Test
    public void anonymousReturnsLicense() throws Exception
    {
        ConnectRunner runner = null;
        try
        {
            LicenseUtils.addPluginLicenses(product);

            final InstallHandlerServlet installHandlerServlet = new InstallHandlerServlet();
            final String productBaseUrl = product.getProductInstance().getBaseUrl();
            runner = new ConnectRunner(productBaseUrl, AddonTestUtils.randomAddOnKey())
                    .addJWT(installHandlerServlet)
                    .enableLicensing()
                    .addScope(ScopeName.READ)
                    .addModule("generalPages", newPageBean()
                            .withKey(AddonTestUtils.randomModuleKey())
                            .withName(new I18nProperty("Hello World", null))
                            .withUrl("/hello_world")
                            .build())
                    .addRoute("/hello_world", ConnectAppServlets.helloWorldServlet())
                    .start();


            URI url = URI.create(productBaseUrl + "/rest/atlassian-connect/1/license");
            url = AddonTestUtils.signWithJwt(url, runner.getAddon().getKey(), installHandlerServlet.getInstallPayload().getSharedSecret(), productBaseUrl, null);
            HttpURLConnection yc = (HttpURLConnection) url.toURL().openConnection();
            yc.setRequestMethod("GET");

            assertNotNull(yc.getResponseCode());
            assertEquals(200, yc.getResponseCode());

            String responseText = IOUtils.toString(yc.getInputStream());
            Gson gson = new Gson();

            LicenseDetailsRepresentation lic = gson.fromJson(responseText, LicenseDetailsRepresentation.class);

            assertTrue(lic.isValid());
        }
        finally
        {
            try
            {
                //NOTE: the timebomb license disables the ability to delete plugins!
                LicenseUtils.resetLicenses(product);
            }
            finally
            {
                if (null != runner)
                {
                    runner.stopAndUninstall();
                }
            }
        }
    }
}
