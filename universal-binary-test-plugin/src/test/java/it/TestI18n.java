package it;

import com.atlassian.plugin.remotable.junit.Mode;
import com.atlassian.plugin.remotable.junit.UniversalBinaries;
import com.atlassian.plugin.remotable.junit.UniversalBinariesContainerJUnitRunner;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(UniversalBinariesContainerJUnitRunner.class)
@UniversalBinaries(value = "${moduleDir}/target/remotable-plugins-universal-binary-test-plugin.jar", mode = Mode.CONTAINER)
public final class TestI18n extends AbstractBrowserlessTest
{
    @Test
    public void testSomething() throws InterruptedException, IOException
    {
        URL url = new URL("http://localhost:8000/sample-ub-java/i18n?message=foo");
        HttpURLConnection yc = (HttpURLConnection) url.openConnection();
        assertEquals(HttpStatus.SC_OK, yc.getResponseCode());
        String body = IOUtils.toString(yc.getInputStream());
        assertTrue("bar".equals(body));
    }
}
