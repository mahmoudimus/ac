package it;

import java.net.HttpURLConnection;
import java.net.URL;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.plugin.rest.license.LicenseDetailsRepresentation;
import com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner;
import com.atlassian.plugin.connect.test.server.module.Condition;
import com.atlassian.plugin.connect.test.server.module.DialogPageModule;
import com.atlassian.plugin.connect.test.server.module.GeneralPageModule;

import com.google.gson.Gson;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import static it.TestConstants.ADMIN_USERNAME;
import static com.atlassian.plugin.connect.test.server.AtlassianConnectAddOnRunner.newMustacheServlet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @since version
 */
public class TestLicenseRestResource extends AbstractRemotablePluginTest
{
    @Test
    public void anonymousReturnsLicense() throws Exception
    {
        addPluginLicenses();
        
        AtlassianConnectAddOnRunner runner = new AtlassianConnectAddOnRunner(product.getProductInstance().getBaseUrl())
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

        //NOTE: we can't just call stop because the timebomb license disables the ability to delete plugins!
        runner.stopRunnerServer();
        
    }
}
