package it.refapp;

import com.atlassian.labs.remoteapps.junit.Mode;
import com.atlassian.labs.remoteapps.junit.UniversalBinaries;
import com.atlassian.labs.remoteapps.junit.UniversalBinariesContainerJUnitRunner;
import com.atlassian.labs.remoteapps.test.GeneralPage;
import com.atlassian.labs.remoteapps.test.OAuthUtils;
import com.atlassian.labs.remoteapps.test.RemoteAppAwarePage;
import com.atlassian.labs.remoteapps.test.RemoteAppTestPage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import it.AbstractRemoteAppTest;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.twdata.pkgscanner.ExportPackageListBuilder;

import static org.junit.Assert.*;

@RunWith(UniversalBinariesContainerJUnitRunner.class)
@UniversalBinaries(value = "${moduleDir}/src/js-sample-app", mode = Mode.CONTAINER)
public final class TestJavaScriptSampleApp extends AbstractRemoteAppTest
{
    @BeforeClass
    public static void before()
    {
        Logger.getLogger(ExportPackageListBuilder.class).setLevel(Level.ERROR);
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
        assertTrue("OK".equals(statusText));
        String contentType = remoteAppTest.getServerHttpContentType();
        assertTrue(contentType != null && contentType.startsWith("text/plain")); // startsWith accounts for possible charset
        assertEquals("betty", remoteAppTest.getServerHttpEntity());

        // basic tests of the RA.request API
        assertEquals("200", remoteAppTest.getClientHttpStatus());
        statusText = remoteAppTest.getClientHttpStatusText();
        assertTrue("OK".equals(statusText) || "success".equals(statusText)); // differs by jquery version
        contentType = remoteAppTest.getClientHttpContentType();
        assertTrue(contentType != null && contentType.startsWith("text/plain")); // startsWith accounts for possible charset
        assertEquals("betty", remoteAppTest.getClientHttpData());
        assertEquals("betty", remoteAppTest.getClientHttpResponseText());
    }
}
