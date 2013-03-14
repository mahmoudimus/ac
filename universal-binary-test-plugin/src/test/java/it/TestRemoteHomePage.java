package it;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.remotable.junit.Mode;
import com.atlassian.plugin.remotable.junit.UniversalBinaries;
import com.atlassian.plugin.remotable.junit.UniversalBinariesContainerJUnitRunner;
import com.atlassian.plugin.remotable.test.GeneralPage;
import com.atlassian.plugin.remotable.test.RemotePluginAwarePage;
import com.atlassian.plugin.remotable.test.RemotePluginTestPage;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(UniversalBinariesContainerJUnitRunner.class)
@UniversalBinaries(value = "${moduleDir}/target/remotable-plugins-universal-binary-test-plugin.jar", mode = Mode.CONTAINER)
public final class TestRemoteHomePage extends TestHomePage
{
    @Override
    protected String getBaseUrl()
    {
        return "http://localhost:8000/sample-ub-java/";
    }

    @Test
    public void testDescriptorServing() throws InterruptedException, IOException
    {
        URL url = new URL(getBaseUrl());
        HttpURLConnection yc = (HttpURLConnection) url.openConnection();
        yc.addRequestProperty("Accept", "application/xml");
        assertEquals(HttpStatus.SC_OK, yc.getResponseCode());
        String body = IOUtils.toString(yc.getInputStream());
        assertTrue(body.contains("<atlassian-plugin"));
    }
}
