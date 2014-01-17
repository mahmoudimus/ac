package it;

import com.atlassian.plugin.connect.plugin.rest.license.LicenseDetailsRepresentation;
import com.atlassian.plugin.connect.test.LicenseUtils;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;

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
        AtlassianConnectAddOnRunner runner = null;
        try
        {
            LicenseUtils.addPluginLicenses(product);

            runner = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
            .addOAuth()
            .enableLicensing()
            .addPermission("read_license")
            .start();


            URL url = new URL(product.getProductInstance().getBaseUrl() + "/rest/atlassian-connect/1/license");
            HttpURLConnection yc = (HttpURLConnection) url.openConnection();

            yc.setRequestMethod("GET");
            runner.getSignedRequestHandler().get().sign(url.toURI(), "GET", null, yc);

            assertNotNull(yc.getResponseCode());
            assertEquals(200, yc.getResponseCode());

            String responseText = IOUtils.toString(yc.getInputStream());
            Gson gson = new Gson();

            LicenseDetailsRepresentation lic = gson.fromJson(responseText, LicenseDetailsRepresentation.class);

            assertTrue(lic.isValid());
        }
        finally
        {
            //NOTE: the timebomb license disables the ability to delete plugins!
            LicenseUtils.resetLicenses(product);

            if(null != runner)
            {
                runner.stopAndUninstall();
            }
        }

    }
}
