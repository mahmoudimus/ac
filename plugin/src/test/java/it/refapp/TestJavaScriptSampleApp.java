package it.refapp;

import com.atlassian.labs.remoteapps.container.Main;
import com.atlassian.labs.remoteapps.test.GeneralPage;
import com.atlassian.labs.remoteapps.test.OAuthUtils;
import com.atlassian.labs.remoteapps.test.RemoteAppAwarePage;
import com.atlassian.labs.remoteapps.test.RemoteAppTestPage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import it.AbstractRemoteAppTest;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.twdata.pkgscanner.ExportPackageListBuilder;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestJavaScriptSampleApp extends AbstractRemoteAppTest
{
    private static Main server;

    @BeforeClass
    public static void before()
        throws Exception
    {
        Logger.getLogger(ExportPackageListBuilder.class).setLevel(Level.ERROR);
        URL url = TestJavaScriptSampleApp.class.getClassLoader().getResource(TestJavaScriptSampleApp.class.getName().replace('.', '/') + ".class");
        File file = new File(url.toURI());
        File srcDir = new File(file.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile(), "src");
        File sampleDir = new File(srcDir, "js-sample-app");
        server = new Main(new String[] {sampleDir.getAbsolutePath()});
    }

    @Test
    public void testContainerSampleGeneralPage()
    {
        product.visit(LoginPage.class).login("betty", "betty", HomePage.class);
        RemoteAppAwarePage page = product.getPageBinder().bind(GeneralPage.class, "general", "Container Sample General");
        assertTrue(page.isRemoteAppLinkPresent());
        RemoteAppTestPage remoteAppTest = page.clickRemoteAppLink();
        assertTrue(remoteAppTest.getTitle().contains("Container Sample General"));
        assertEquals("Success", remoteAppTest.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), remoteAppTest.getConsumerKey());
        assertEquals("Betty Admin", remoteAppTest.getFullName());
        assertEquals("betty", remoteAppTest.getUserId());

        // basic tests of the HostHttpClient API
        assertEquals("200", remoteAppTest.getServerHttpStatus());
        String statusText = remoteAppTest.getServerHttpStatusText();
        assertTrue("OK".equals(statusText) || "success".equals(statusText)); // differs by jquery version
        String contentType = remoteAppTest.getServerHttpContentType();
        assertTrue(contentType != null && contentType.startsWith("text/plain")); // startsWith accounts for possible encoding
        assertEquals("betty", remoteAppTest.getServerHttpEntity());

        // basic tests of the RA.request API
        assertEquals("200", remoteAppTest.getClientHttpStatus());
        statusText = remoteAppTest.getClientHttpStatusText();
        assertTrue("OK".equals(statusText) || "success".equals(statusText)); // differs by jquery version
        contentType = remoteAppTest.getClientHttpContentType();
        assertTrue(contentType != null && contentType.startsWith("text/plain")); // startsWith accounts for possible encoding
        assertEquals("betty", remoteAppTest.getClientHttpData());
        assertEquals("betty", remoteAppTest.getClientHttpResponseText());
    }

    @AfterClass
    public static void after()
    {
        server.stop();
    }
}
