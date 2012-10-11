package it.refapp;

import com.atlassian.plugin.remotable.junit.Mode;
import com.atlassian.plugin.remotable.junit.UniversalBinaries;
import com.atlassian.plugin.remotable.junit.UniversalBinariesContainerJUnitRunner;
import com.atlassian.plugin.remotable.test.GeneralPage;
import com.atlassian.plugin.remotable.test.OAuthUtils;
import com.atlassian.plugin.remotable.test.RemotePluginAwarePage;
import com.atlassian.plugin.remotable.test.RemotePluginTestPage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import it.AbstractRemotablePluginTest;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.twdata.pkgscanner.ExportPackageListBuilder;

import static org.junit.Assert.*;

@RunWith(UniversalBinariesContainerJUnitRunner.class)
@UniversalBinaries(value = "${moduleDir}/src/js-sample-app", mode = Mode.CONTAINER)
public final class TestJavaScriptSampleApp extends AbstractRemotablePluginTest
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
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "general", "Container Sample General");
        assertTrue(page.isRemotePluginLinkPresent());
        RemotePluginTestPage remotePluginTest = page.clickRemotePluginLink();
        assertTrue(remotePluginTest.getTitle().contains("Container Sample General"));
        assertEquals("Success", remotePluginTest.getMessage());
        assertEquals(OAuthUtils.getConsumerKey(), remotePluginTest.getConsumerKey());
        assertEquals("Betty Admin", remotePluginTest.getFullName());
        assertEquals("betty", remotePluginTest.getUserId());

        // basic tests of the HostHttpClient API
        assertEquals("200", remotePluginTest.getServerHttpStatus());
        String statusText = remotePluginTest.getServerHttpStatusText();
        assertTrue("OK".equals(statusText));
        String contentType = remotePluginTest.getServerHttpContentType();
        assertTrue(contentType != null && contentType.startsWith("text/plain")); // startsWith accounts for possible charset
        assertEquals("betty", remotePluginTest.getServerHttpEntity());

        // basic tests of the RA.request API
        assertEquals("200", remotePluginTest.getClientHttpStatus());
        statusText = remotePluginTest.getClientHttpStatusText();
        assertTrue("OK".equals(statusText) || "success".equals(statusText)); // differs by jquery version
        contentType = remotePluginTest.getClientHttpContentType();
        assertTrue(contentType != null && contentType.startsWith("text/plain")); // startsWith accounts for possible charset
        assertEquals("betty", remotePluginTest.getClientHttpData());
        assertEquals("betty", remotePluginTest.getClientHttpResponseText());
    }
}
