package it.modules;

import com.atlassian.plugin.connect.plugin.rest.license.LicenseDetailsRepresentation;
import com.atlassian.plugin.connect.test.LicenseUtils;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.google.gson.Gson;
import it.ConnectWebDriverTestBase;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.*;

//TODO: we need to implement permissions before we can use OAuth and Licensing. Once we can request permissions in a json descriptor, this should be put back in
@Ignore
public class TestLicenseResource extends ConnectWebDriverTestBase
{

    public static final String PLUGIN_KEY = "i-am-licensed";

    @Test
    public void anonymousReturnsLicense() throws Exception
    {
        ConnectRunner runner = null;
        try
        {
            LicenseUtils.addPluginLicenses(product);

            runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), PLUGIN_KEY)
                    .addOAuth()
                    .enableLicensing()
                    .start();

            URL url = new URL(product.getProductInstance().getBaseUrl() + "/rest/atlassian-connect/1/license");
            HttpURLConnection yc = (HttpURLConnection) url.openConnection();

            yc.setRequestMethod("GET");
            ConnectRunner.createSignedRequestHandler(PLUGIN_KEY).sign(url.toURI(), "GET", null, yc);

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
